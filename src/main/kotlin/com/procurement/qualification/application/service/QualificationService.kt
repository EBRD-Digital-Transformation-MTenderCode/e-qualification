package com.procurement.qualification.application.service

import com.procurement.qualification.application.model.params.CheckAccessToQualificationParams
import com.procurement.qualification.application.model.params.CheckDeclarationParams
import com.procurement.qualification.application.model.params.CheckQualificationStateParams
import com.procurement.qualification.application.model.params.CreateQualificationsParams
import com.procurement.qualification.application.model.params.DoConsiderationParams
import com.procurement.qualification.application.model.params.DoDeclarationParams
import com.procurement.qualification.application.model.params.FindQualificationIdsParams
import com.procurement.qualification.application.model.params.FindRequirementResponseByIdsParams
import com.procurement.qualification.application.model.params.RankQualificationsParams
import com.procurement.qualification.application.repository.QualificationRepository
import com.procurement.qualification.domain.enums.ConversionRelatesTo
import com.procurement.qualification.domain.enums.QualificationStatus
import com.procurement.qualification.domain.enums.QualificationStatusDetails
import com.procurement.qualification.domain.enums.QualificationSystemMethod
import com.procurement.qualification.domain.enums.ReductionCriteria
import com.procurement.qualification.domain.enums.RequirementDataType
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.ValidationResult
import com.procurement.qualification.domain.functional.asFailure
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.functional.asValidationFailure
import com.procurement.qualification.domain.model.measure.Scoring
import com.procurement.qualification.domain.model.qualification.Qualification
import com.procurement.qualification.domain.model.qualification.QualificationId
import com.procurement.qualification.domain.model.requirement.RequirementResponseValue
import com.procurement.qualification.domain.model.tender.conversion.coefficient.CoefficientRate
import com.procurement.qualification.domain.model.tender.conversion.coefficient.CoefficientValue
import com.procurement.qualification.domain.util.extension.getNewElements
import com.procurement.qualification.domain.util.extension.getUnknownElements
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.fail.error.ValidationError
import com.procurement.qualification.infrastructure.handler.check.qualification.protocol.CheckQualificationsForProtocolParams
import com.procurement.qualification.infrastructure.handler.create.consideration.DoConsiderationResult
import com.procurement.qualification.infrastructure.handler.create.declaration.DoDeclarationResult
import com.procurement.qualification.infrastructure.handler.create.qualifications.CreateQualificationsResult
import com.procurement.qualification.infrastructure.handler.determine.nextforqualification.RankQualificationsResult
import com.procurement.qualification.infrastructure.handler.find.requirementresponsebyids.FindRequirementResponseByIdsResult
import org.springframework.stereotype.Service
import java.math.BigDecimal

interface QualificationService {

    fun findQualificationIds(params: FindQualificationIdsParams): Result<List<QualificationId>, Fail>
    fun createQualifications(params: CreateQualificationsParams): Result<List<CreateQualificationsResult>, Fail.Incident>
    fun rankQualifications(params: RankQualificationsParams): Result<List<RankQualificationsResult>, Fail>
    fun checkAccessToQualification(params: CheckAccessToQualificationParams): ValidationResult<Fail>
    fun checkQualificationState(params: CheckQualificationStateParams): ValidationResult<Fail>
    fun doDeclaration(params: DoDeclarationParams): Result<DoDeclarationResult, Fail>
    fun checkDeclaration(params: CheckDeclarationParams): ValidationResult<Fail>
    fun doConsideration(params: DoConsiderationParams): Result<DoConsiderationResult, Fail>
    fun findRequirementResponseByIds(params: FindRequirementResponseByIdsParams): Result<FindRequirementResponseByIdsResult?, Fail>
    fun checkQualificationsForProtocol(params: CheckQualificationsForProtocolParams): ValidationResult<Fail>
}

@Service
class QualificationServiceImpl(
    val qualificationRepository: QualificationRepository,
    val generationService: GenerationService,
    val rulesService: RulesService
) : QualificationService {

    override fun findQualificationIds(params: FindQualificationIdsParams): Result<List<QualificationId>, Fail> {

        val qualifications = qualificationRepository.findBy(cpid = params.cpid, ocid = params.ocid)
            .orForwardFail { fail -> return fail }

        if (params.states.isEmpty())
            return qualifications.map { it.id }
                .asSuccess()

        val filteredQualifications = qualifications.filter { qualification ->
            compareStatuses(qualification = qualification, states = params.states)
        }

        return filteredQualifications.map { it.id }
            .asSuccess()
    }

    override fun createQualifications(params: CreateQualificationsParams): Result<List<CreateQualificationsResult>, Fail.Incident> {

        val qualifications = params.submissions
            .map { submission ->
                val scoring: Scoring? = calculateScoring(submission = submission, params = params)

                Qualification(
                    id = QualificationId.generate(),
                    date = params.date,
                    owner = params.owner,
                    relatedSubmission = submission.id,
                    token = generationService.generateToken(),
                    status = QualificationStatus.PENDING,
                    scoring = scoring
                )

            }

        qualificationRepository.saveAll(params.cpid, params.ocid, qualifications)

        return qualifications.map { qualification ->
            CreateQualificationsResult(
                scoring = qualification.scoring,
                id = qualification.id,
                status = qualification.status,
                token = qualification.token,
                relatedSubmission = qualification.relatedSubmission,
                date = qualification.date
            )
        }
            .asSuccess()
    }

    override fun rankQualifications(params: RankQualificationsParams): Result<List<RankQualificationsResult>, Fail> {

        val cpid = params.cpid
        val ocid = params.ocid

        val qualifications = qualificationRepository.findBy(cpid = cpid, ocid = ocid)
            .orForwardFail { fail -> return fail }

        if (qualifications.isEmpty())
            return ValidationError.QualificationsNotFoundOnRankQualifications(cpid = cpid, ocid = ocid)
                .asFailure()

        val filteredQualifications = filterByRelatedSubmissions(
            qualifications = qualifications,
            submissions = params.submissions
        )
            .orForwardFail { fail -> return fail }

        val reductionCriteria = params.tender.otherCriteria.reductionCriteria
        val qualificationSystemMethod = params.tender.otherCriteria.qualificationSystemMethod
        val criteria = params.tender.criteria

        val updatedQualifications = when (reductionCriteria) {
            ReductionCriteria.SCORING -> {
                when (qualificationSystemMethod) {
                    QualificationSystemMethod.AUTOMATED -> {
                        val qualificationWithMinScoring = findMinScoring(qualifications = filteredQualifications)!!
                        val hasSameScoring = hasSameScoring(
                            qualifications = filteredQualifications,
                            scoring = qualificationWithMinScoring.scoring!!
                        )
                        val qualificationsToUpdate =
                            if (hasSameScoring) {
                                val submissionWithMinDate = findMinDate(submissions = params.submissions)!!
                                val qualificationRelatedToSubmission = filteredQualifications.find { q -> q.relatedSubmission == submissionWithMinDate.id }!!
                                listOf(qualificationRelatedToSubmission)
                            } else {
                                listOf(qualificationWithMinScoring)
                            }

                        setStatusDetailsByCriteria(criteria = criteria, qualifications = qualificationsToUpdate)
                    }
                    QualificationSystemMethod.MANUAL ->
                        setStatusDetailsByCriteria(criteria = criteria, qualifications = filteredQualifications)
                }
            }
            ReductionCriteria.NONE -> {
                when (qualificationSystemMethod) {
                    QualificationSystemMethod.AUTOMATED,
                    QualificationSystemMethod.MANUAL ->
                        setStatusDetailsByCriteria(criteria = criteria, qualifications = filteredQualifications)
                }
            }
        }

        qualificationRepository.updateAll(params.cpid, params.ocid, updatedQualifications)
            .doOnFail { fail -> return fail.asFailure() }

        return updatedQualifications
            .map { qualification ->
                RankQualificationsResult(id = qualification.id, statusDetails = qualification.statusDetails)
            }
            .asSuccess()
    }

    override fun checkAccessToQualification(params: CheckAccessToQualificationParams): ValidationResult<Fail> {

        val cpid = params.cpid
        val ocid = params.ocid
        val qualificationId = params.qualificationId

        val qualification = qualificationRepository.findBy(
            cpid = cpid,
            ocid = ocid,
            qualificationId = qualificationId
        )
            .doReturn { fail -> return ValidationResult.error(fail) }
            ?: return ValidationError.QualificationNotFoundFor.CheckAccessToQualification(
                cpid = cpid,
                ocid = ocid,
                qualificationId = qualificationId
            )
                .asValidationFailure()

        if (params.token != qualification.token)
            return ValidationError.InvalidTokenOnCheckAccessToQualification(cpid = params.cpid, token = params.token)
                .asValidationFailure()

        if (params.owner != qualification.owner)
            return ValidationError.InvalidOwnerOnCheckAccessToQualification(cpid = params.cpid, owner = params.owner)
                .asValidationFailure()

        return ValidationResult.ok()
    }

    override fun checkQualificationState(params: CheckQualificationStateParams): ValidationResult<Fail> {

        val cpid = params.cpid
        val ocid = params.ocid
        val qualificationId = params.qualificationId

        val qualification = qualificationRepository.findBy(
            cpid = cpid,
            ocid = ocid,
            qualificationId = qualificationId
        )
            .doReturn { fail -> return ValidationResult.error(fail) }
            ?: return ValidationError.QualificationNotFoundFor.CheckQualificationState(
                cpid = cpid,
                ocid = ocid,
                qualificationId = qualificationId
            )
                .asValidationFailure()

        val states = rulesService.findValidStates(
            country = params.country,
            operationType = params.operationType,
            pmd = params.pmd
        )
            .doReturn { fail -> return ValidationResult.error(fail) }

        states.find { it.status == qualification.status && it.statusDetails == qualification.statusDetails }
            ?: return ValidationError.QualificationStatesIsInvalidOnCheckQualificationState(qualificationId = qualification.id)
                .asValidationFailure()

        return ValidationResult.ok()
    }

    override fun doDeclaration(params: DoDeclarationParams): Result<DoDeclarationResult, Fail> {
        val cpid = params.cpid
        val ocid = params.ocid

        val qualificationsFromDb = qualificationRepository.findBy(cpid = cpid, ocid = ocid)
            .orForwardFail { fail -> return fail }
        val qualificationFromDbById = qualificationsFromDb.associateBy { it.id }
        val qualifications = params.qualifications
            .map { qualification ->
                qualificationFromDbById[qualification.id]
                    ?: return ValidationError.QualificationNotFoundFor.DoDeclaration(
                        cpid = cpid,
                        ocid = ocid,
                        qualificationId = qualification.id
                    ).asFailure()
            }

        val rqQualificationById = params.qualifications.associateBy { it.id }
        val updatedQualifications = qualifications
            .map { qualification ->
                val rqRequirementResponseById = rqQualificationById.getValue(qualification.id)
                    .requirementResponses
                    .associateBy { it.id }
                val requirementResponseById = qualification.requirementResponses
                    .associateBy { it.id }
                val updatedRequirementResponses = requirementResponseById
                    .map { (id, requirementResponse) ->
                        rqRequirementResponseById[id]
                            ?.let {
                                requirementResponse.copy(value = it.value)
                            }
                            ?: requirementResponse
                    }

                val newRequirementResponses =
                    getNewElements(received = rqRequirementResponseById.keys, known = requirementResponseById.keys)
                        .map { id ->
                            buildRequirementResponse(requirementResponse = rqRequirementResponseById.getValue(id))
                        }

                qualification.copy(requirementResponses = updatedRequirementResponses + newRequirementResponses)
            }

        qualificationRepository.updateAll(cpid, ocid, updatedQualifications)
            .doOnFail { fail -> return fail.asFailure() }

        return updatedQualifications.convertQualificationsToDoDeclarationResult()
            .asSuccess()
    }

    override fun checkDeclaration(params: CheckDeclarationParams): ValidationResult<Fail> {

        val cpid = params.cpid
        val ocid = params.ocid
        val qualificationId = params.qualificationId

        val qualification = qualificationRepository.findBy(
            cpid = cpid,
            ocid = ocid,
            qualificationId = qualificationId
        )
            .doReturn { fail -> return ValidationResult.error(fail) }
            ?: return ValidationError.QualificationNotFoundFor.CheckDeclaration(
                cpid = cpid,
                ocid = ocid,
                qualificationId = qualificationId
            )
                .asValidationFailure()

        val requirement = params.criteria
            .asSequence()
            .flatMap {
                it.requirementGroups.asSequence()
            }
            .flatMap {
                it.requirements.asSequence()
            }
            .find { it.id == params.requirementResponse.requirementId }
            ?: return ValidationError.RequirementNotFoundOnCheckDeclaration(requirementId = params.requirementResponse.requirementId)
                .asValidationFailure()

        if (!isMatchingDataType(datatype = requirement.dataType, value = params.requirementResponse.value))
            return ValidationError.ValueDataTypeMismatchOnCheckDeclaration(
                actual = params.requirementResponse.value,
                expected = requirement.dataType
            )
                .asValidationFailure()

        qualification.requirementResponses
            .find {
                it.responder.id == params.requirementResponse.responderId
                    && it.relatedTenderer.id == params.requirementResponse.relatedTendererId
                    && it.requirement.id == params.requirementResponse.requirementId
            }
            ?.apply {
                if (this.id != params.requirementResponse.id)
                    return ValidationError.InvalidRequirementResponseIdOnCheckDeclaration(
                        expected = this.id,
                        actualId = params.requirementResponse.id
                    )
                        .asValidationFailure()
            }

        return ValidationResult.ok()
    }

    override fun findRequirementResponseByIds(params: FindRequirementResponseByIdsParams): Result<FindRequirementResponseByIdsResult?, Fail> {

        val cpid = params.cpid
        val ocid = params.ocid
        val qualificationId = params.qualificationId

        val qualification = qualificationRepository.findBy(
            cpid = cpid,
            ocid = ocid,
            qualificationId = qualificationId
        )
            .orForwardFail { fail -> return fail }
            ?: return ValidationError.QualificationNotFoundFor.FindRequirementResponseByIds(cpid, ocid, qualificationId)
                .asFailure()

        val rqRequirementResponsesByIds = params.requirementResponseIds
            .associateBy { it }

        val filteredRequirementResponses = qualification.requirementResponses
            .filter { rqRequirementResponsesByIds.containsKey(it.id) }

        return filteredRequirementResponses
            .takeIf { it.isNotEmpty() }
            ?.let {
                FindRequirementResponseByIdsResult(
                    qualification = FindRequirementResponseByIdsResult.Qualification(
                        id = params.qualificationId,
                        requirementResponses = filteredRequirementResponses.map { requirementResponse ->
                            requirementResponse.convertToFindRequirementResponseByIdsResultRR()
                        }
                    )
                )
            }
            .asSuccess<FindRequirementResponseByIdsResult?, Fail>()
    }

    override fun doConsideration(params: DoConsiderationParams): Result<DoConsiderationResult, Fail> {
        val requestQualificationIds = params.qualifications.map { it.id }
        val qualifications = qualificationRepository.findBy(
            cpid = params.cpid, ocid = params.ocid, qualificationIds = requestQualificationIds
        ).orForwardFail { fail -> return fail }

        val unknownElements = getUnknownElements(
            received = requestQualificationIds,
            known = qualifications.map { it.id })

        if (unknownElements.isNotEmpty())
            return ValidationError.QualificationNotFoundFor.DoConsideration(
                cpid = params.cpid, ocid = params.ocid, qualificationId = unknownElements.first()
            ).asFailure()

        val updatedQualifications = qualifications.map { qualification ->
            qualification.copy(statusDetails = QualificationStatusDetails.CONSIDERATION)
        }

        qualificationRepository.updateAll(
            cpid = params.cpid, ocid = params.ocid, qualifications = updatedQualifications
        )

        return DoConsiderationResult(qualifications = updatedQualifications.map { updatedQualification ->
            DoConsiderationResult.Qualification(
                id = updatedQualification.id,
                statusDetails = updatedQualification.statusDetails!!
            )
        }).asSuccess()
    }

    override fun checkQualificationsForProtocol(params: CheckQualificationsForProtocolParams): ValidationResult<Fail> {
        val cpid = params.cpid
        val ocid = params.ocid
        val qualifications = qualificationRepository.findBy(cpid = cpid, ocid = ocid)
            .doReturn { error -> return ValidationResult.error(error) }

        if (qualifications.isEmpty())
            return ValidationError.QualificationNotFoundFor.CheckQualificationsForProtocol(cpid = cpid, ocid = ocid)
                .asValidationFailure()

        val allowedStatusDetails = setOf(QualificationStatusDetails.ACTIVE, QualificationStatusDetails.UNSUCCESSFUL)

        val unsuitableQualification = qualifications.find { qualification ->
            qualification.status != QualificationStatus.PENDING
                || qualification.statusDetails !in allowedStatusDetails
        }

        if (unsuitableQualification != null)
            return ValidationError.UnsuitableQualificationFound(cpid, ocid, unsuitableQualification.id)
                .asValidationFailure()

        return ValidationResult.ok()
    }

    private fun filterByRelatedSubmissions(
        qualifications: List<Qualification>,
        submissions: List<RankQualificationsParams.Submission>
    ): Result<List<Qualification>, ValidationError.RelatedSubmissionNotEqualOnRankQualifications> {

        val qualificationByRelatedSubmission = qualifications.associateBy { it.relatedSubmission }
        return submissions.map {
            qualificationByRelatedSubmission[it.id]
                ?: return ValidationError.RelatedSubmissionNotEqualOnRankQualifications(
                    submissionId = it.id
                )
                    .asFailure()
        }
            .asSuccess()
    }

    private fun setStatusDetailsByCriteria(
        qualifications: List<Qualification>,
        criteria: List<RankQualificationsParams.Tender.Criteria>?
    ) = if (criteria.isNullOrEmpty()) {
        setStatusDetails(
            statusDetails = QualificationStatusDetails.CONSIDERATION,
            qualifications = qualifications
        )
    } else {
        setStatusDetails(
            statusDetails = QualificationStatusDetails.AWAITING,
            qualifications = qualifications
        )
    }

    private fun calculateScoring(
        submission: CreateQualificationsParams.Submission,
        params: CreateQualificationsParams
    ): Scoring? = when (params.tender.otherCriteria.reductionCriteria) {
        ReductionCriteria.SCORING -> {
            when (params.tender.otherCriteria.qualificationSystemMethod) {
                QualificationSystemMethod.MANUAL -> null
                QualificationSystemMethod.AUTOMATED -> {
                    calculateAutomatedScoring(submission = submission, conversions = params.tender.conversions)
                }
            }
        }
        ReductionCriteria.NONE -> null
    }

    private fun calculateAutomatedScoring(
        conversions: List<CreateQualificationsParams.Tender.Conversion>,
        submission: CreateQualificationsParams.Submission
    ): Scoring {
        val conversionsRelatesToRequirement = conversions
            .filter { it.relatesTo == ConversionRelatesTo.REQUIREMENT }
            .associateBy { it.relatedItem }

        return Scoring.invoke(
            value = submission.requirementResponses
                .map { requirementResponse ->
                    conversionsRelatesToRequirement[requirementResponse.requirement.id.toString()]
                        ?.coefficients
                        ?.filter {
                            isMatchCoefficientValueAndRequirementValue(
                                coefficientValue = it.value,
                                requirementValue = requirementResponse.value
                            )
                        }
                        ?.map { it.coefficient }
                        ?.reduce { start, next -> start * next }
                        ?: CoefficientRate(BigDecimal.ONE)
                }
                .reduce { start, next -> start * next }
                .rate
        )
    }

    private fun findMinScoring(qualifications: List<Qualification>) = qualifications.minBy { it.scoring!! }
    private fun hasSameScoring(qualifications: List<Qualification>, scoring: Scoring) = qualifications
        .filter { scoring == it.scoring }
        .count() > 1

    private fun findMinDate(submissions: List<RankQualificationsParams.Submission>) =
        submissions.minBy { it.date }

    private fun setStatusDetails(statusDetails: QualificationStatusDetails, qualifications: List<Qualification>) =
        qualifications.map { it.copy(statusDetails = statusDetails) }

    private fun isMatchCoefficientValueAndRequirementValue(
        coefficientValue: CoefficientValue,
        requirementValue: RequirementResponseValue
    ): Boolean = when (coefficientValue) {
        is CoefficientValue.AsBoolean -> when (requirementValue) {
            is RequirementResponseValue.AsBoolean -> coefficientValue.value == requirementValue.value
            is RequirementResponseValue.AsString,
            is RequirementResponseValue.AsNumber,
            is RequirementResponseValue.AsInteger -> false
        }
        is CoefficientValue.AsString -> when (requirementValue) {
            is RequirementResponseValue.AsString -> coefficientValue.value == requirementValue.value
            is RequirementResponseValue.AsBoolean,
            is RequirementResponseValue.AsNumber,
            is RequirementResponseValue.AsInteger -> false
        }
        is CoefficientValue.AsNumber -> when (requirementValue) {
            is RequirementResponseValue.AsNumber -> coefficientValue.value == requirementValue.value
            is RequirementResponseValue.AsBoolean,
            is RequirementResponseValue.AsString,
            is RequirementResponseValue.AsInteger -> false
        }
        is CoefficientValue.AsInteger -> when (requirementValue) {
            is RequirementResponseValue.AsInteger -> coefficientValue.value == requirementValue.value
            is RequirementResponseValue.AsBoolean,
            is RequirementResponseValue.AsNumber,
            is RequirementResponseValue.AsString -> false
        }
    }

    private fun Qualification.RequirementResponse.convertToFindRequirementResponseByIdsResultRR() =
        this.let { requirementResponse ->
            FindRequirementResponseByIdsResult.Qualification.RequirementResponse(
                id = requirementResponse.id,
                value = requirementResponse.value,
                relatedTenderer = requirementResponse.relatedTenderer
                    .let {
                        FindRequirementResponseByIdsResult.Qualification.RequirementResponse.RelatedTenderer(
                            id = it.id
                        )
                    },
                requirement = requirementResponse.requirement
                    .let { FindRequirementResponseByIdsResult.Qualification.RequirementResponse.Requirement(id = it.id) },
                responder = requirementResponse.responder
                    .let {
                        FindRequirementResponseByIdsResult.Qualification.RequirementResponse.Responder(
                            id = it.id,
                            name = it.name
                        )
                    }
            )
        }

    private fun isMatchingDataType(datatype: RequirementDataType, value: RequirementResponseValue) =
        when (value) {
            is RequirementResponseValue.AsString -> datatype == RequirementDataType.STRING
            is RequirementResponseValue.AsBoolean -> datatype == RequirementDataType.BOOLEAN
            is RequirementResponseValue.AsNumber -> datatype == RequirementDataType.NUMBER
            is RequirementResponseValue.AsInteger -> datatype == RequirementDataType.INTEGER
        }

    private fun List<Qualification>.convertQualificationsToDoDeclarationResult(): DoDeclarationResult =
        DoDeclarationResult(
            qualifications = this.map { qualification ->
                DoDeclarationResult.Qualification(
                    id = qualification.id,
                    requirementResponses = qualification.requirementResponses
                        .map { rr ->
                            DoDeclarationResult.Qualification.RequirementResponse(
                                id = rr.id,
                                value = rr.value,
                                relatedTenderer = rr.relatedTenderer
                                    .let { DoDeclarationResult.Qualification.RequirementResponse.RelatedTenderer(id = it.id) },
                                requirement = rr.requirement
                                    .let { DoDeclarationResult.Qualification.RequirementResponse.Requirement(id = it.id) },
                                responder = rr.responder
                                    .let {
                                        DoDeclarationResult.Qualification.RequirementResponse.Responder(
                                            id = it.id,
                                            name = it.name
                                        )
                                    }
                            )
                        }
                )
            }
        )

    private fun buildRequirementResponse(requirementResponse: DoDeclarationParams.Qualification.RequirementResponse): Qualification.RequirementResponse =
        Qualification.RequirementResponse(
            id = requirementResponse.id,
            value = requirementResponse.value,
            relatedTenderer = requirementResponse.relatedTenderer
                .let { Qualification.RequirementResponse.RelatedTenderer(id = it.id) },
            requirement = requirementResponse.requirement
                .let { Qualification.RequirementResponse.Requirement(id = it.id) },
            responder = requirementResponse.responder
                .let {
                    Qualification.RequirementResponse.Responder(id = it.id, name = it.name)
                }
        )

    private fun compareStatuses(qualification: Qualification, states: List<FindQualificationIdsParams.State>): Boolean {
        return states.any { state ->
            state.status == qualification.status ||
                state.statusDetails == qualification.statusDetails
        }
    }
}
