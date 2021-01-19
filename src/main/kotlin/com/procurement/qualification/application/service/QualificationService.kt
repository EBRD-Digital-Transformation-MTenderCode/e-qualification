package com.procurement.qualification.application.service

import com.procurement.qualification.application.model.params.AnalyzeQualificationsForInvitationParams
import com.procurement.qualification.application.model.params.CheckAccessToQualificationParams
import com.procurement.qualification.application.model.params.CheckDeclarationParams
import com.procurement.qualification.application.model.params.CheckQualificationStateParams
import com.procurement.qualification.application.model.params.CreateQualificationsParams
import com.procurement.qualification.application.model.params.DoConsiderationParams
import com.procurement.qualification.application.model.params.DoDeclarationParams
import com.procurement.qualification.application.model.params.DoQualificationParams
import com.procurement.qualification.application.model.params.FinalizeQualificationsParams
import com.procurement.qualification.application.model.params.FindQualificationIdsParams
import com.procurement.qualification.application.model.params.FindRequirementResponseByIdsParams
import com.procurement.qualification.application.model.params.RankQualificationsParams
import com.procurement.qualification.application.model.params.SetNextForQualificationParams
import com.procurement.qualification.application.model.params.SetQualificationPeriodEndParams
import com.procurement.qualification.application.repository.PeriodRepository
import com.procurement.qualification.application.repository.QualificationRepository
import com.procurement.qualification.domain.enums.ConversionRelatesTo
import com.procurement.qualification.domain.enums.CriteriaSource
import com.procurement.qualification.domain.enums.QualificationStatus
import com.procurement.qualification.domain.enums.QualificationStatusDetails
import com.procurement.qualification.domain.enums.QualificationSystemMethod
import com.procurement.qualification.domain.enums.ReductionCriteria
import com.procurement.qualification.domain.enums.RequirementDataType
import com.procurement.qualification.domain.functional.Option
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.Result.Companion.failure
import com.procurement.qualification.domain.functional.Result.Companion.success
import com.procurement.qualification.domain.functional.ValidationResult
import com.procurement.qualification.domain.functional.asFailure
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.functional.asValidationFailure
import com.procurement.qualification.domain.model.measure.Scoring
import com.procurement.qualification.domain.model.qualification.Qualification
import com.procurement.qualification.domain.model.qualification.QualificationId
import com.procurement.qualification.domain.model.requirement.RequirementResponseValue
import com.procurement.qualification.domain.model.state.States
import com.procurement.qualification.domain.model.tender.conversion.coefficient.CoefficientRate
import com.procurement.qualification.domain.model.tender.conversion.coefficient.CoefficientValue
import com.procurement.qualification.domain.util.extension.getNewElements
import com.procurement.qualification.domain.util.extension.getUnknownElements
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.fail.error.ValidationError
import com.procurement.qualification.infrastructure.fail.error.ValidationError.PeriodNotFoundFor
import com.procurement.qualification.infrastructure.fail.error.ValidationError.QualificationNotFoundFor
import com.procurement.qualification.infrastructure.fail.error.ValidationError.RelatedSubmissionNotEqualOnSetNextForQualification
import com.procurement.qualification.infrastructure.handler.analyze.qualification.AnalyzeQualificationsForInvitationResult
import com.procurement.qualification.infrastructure.handler.check.qualification.protocol.CheckQualificationsForProtocolParams
import com.procurement.qualification.infrastructure.handler.create.consideration.DoConsiderationResult
import com.procurement.qualification.infrastructure.handler.create.declaration.DoDeclarationResult
import com.procurement.qualification.infrastructure.handler.create.declaration.convertQualificationsToDoDeclarationResult
import com.procurement.qualification.infrastructure.handler.create.qualification.DoQualificationResult
import com.procurement.qualification.infrastructure.handler.create.qualification.convertToDoQualificationResult
import com.procurement.qualification.infrastructure.handler.create.qualifications.CreateQualificationsResult
import com.procurement.qualification.infrastructure.handler.determine.nextforqualification.RankQualificationsResult
import com.procurement.qualification.infrastructure.handler.finalize.FinalizeQualificationsResult
import com.procurement.qualification.infrastructure.handler.find.requirementresponsebyids.FindRequirementResponseByIdsResult
import com.procurement.qualification.infrastructure.handler.find.requirementresponsebyids.convertToFindRequirementResponseByIdsResultRR
import com.procurement.qualification.infrastructure.handler.set.SetQualificationPeriodEndResult
import com.procurement.qualification.infrastructure.handler.set.nextforqualification.SetNextForQualificationResult
import com.procurement.qualification.infrastructure.handler.set.nextforqualification.convertToSetNextForQualification
import com.procurement.qualification.lib.toSetBy
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

interface QualificationService {

    fun findQualificationIds(params: FindQualificationIdsParams): Result<List<QualificationId>, Fail>
    fun finalizeQualifications(params: FinalizeQualificationsParams): Result<FinalizeQualificationsResult, Fail>
    fun createQualifications(params: CreateQualificationsParams): Result<List<CreateQualificationsResult>, Fail.Incident>
    fun rankQualifications(params: RankQualificationsParams): Result<List<RankQualificationsResult>, Fail>
    fun checkAccessToQualification(params: CheckAccessToQualificationParams): ValidationResult<Fail>
    fun checkQualificationState(params: CheckQualificationStateParams): ValidationResult<Fail>
    fun doDeclaration(params: DoDeclarationParams): Result<DoDeclarationResult, Fail>
    fun checkDeclaration(params: CheckDeclarationParams): ValidationResult<Fail>
    fun doConsideration(params: DoConsiderationParams): Result<DoConsiderationResult, Fail>
    fun findRequirementResponseByIds(params: FindRequirementResponseByIdsParams): Result<FindRequirementResponseByIdsResult?, Fail>
    fun setNextForQualification(params: SetNextForQualificationParams): Result<SetNextForQualificationResult?, Fail>
    fun setQualificationPeriodEnd(params: SetQualificationPeriodEndParams): Result<SetQualificationPeriodEndResult, Fail>
    fun doQualification(params: DoQualificationParams): Result<DoQualificationResult, Fail>
    fun checkQualificationsForProtocol(params: CheckQualificationsForProtocolParams): ValidationResult<Fail>
    fun analyzeQualificationsForInvitation(params: AnalyzeQualificationsForInvitationParams): Result<AnalyzeQualificationsForInvitationResult?, Fail>
}

@Service
class QualificationServiceImpl(
    val qualificationRepository: QualificationRepository,
    val periodRepository: PeriodRepository,
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

    override fun finalizeQualifications(params: FinalizeQualificationsParams): Result<FinalizeQualificationsResult, Fail> {
        val qualificationsfromDb = qualificationRepository
            .findBy(params.cpid, params.ocid)
            .map { submissions -> submissions.takeIf { it.isNotEmpty() } }
            .doReturn { fail -> return failure(fail) }
            ?: return failure(QualificationNotFoundFor.FinalizeQualifications(params.cpid, params.ocid))

        val updatedQualifications = qualificationsfromDb.asSequence()
            .map { qualification -> qualification to defineStateToUpdate(qualification.statusDetails) }
            .filter { (_, newState) -> newState.isDefined }
            .map { (qualification, newState) ->
                val (status, statusDetails) = newState.get
                qualification.copy(status = status, statusDetails = statusDetails)
            }
            .toList()

        val result = FinalizeQualificationsResult(
            qualifications = updatedQualifications.map { FinalizeQualificationsResult.fromDomain(it) }
        )

        qualificationRepository.update(params.cpid, params.ocid, updatedQualifications)

        return success(result)
    }

    override fun createQualifications(params: CreateQualificationsParams): Result<List<CreateQualificationsResult>, Fail.Incident> {

        val tender = params.tender
        val isNeedCalculateScoring = isCalculateScoringNeeded(
            tender.otherCriteria.reductionCriteria,
            tender.otherCriteria.qualificationSystemMethod
        )

        val qualifications = params.submissions
            .map { submission ->
                val scoring: Scoring? = if (isNeedCalculateScoring) {
                    val coefficients = getCoefficients(tender.conversions, submission.requirementResponses)
                    calculateScoring(coefficients = coefficients)
                } else
                    null

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

        qualificationRepository.add(params.cpid, params.ocid, qualifications)

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
                        val qualificationsToUpdate = getMinScoringOrMinDateQualifications(filteredQualifications, params)
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

        qualificationRepository.update(params.cpid, params.ocid, updatedQualifications)
            .doOnFail { fail -> return fail.asFailure() }

        return updatedQualifications
            .map { qualification ->
                RankQualificationsResult(id = qualification.id, statusDetails = qualification.statusDetails)
            }
            .asSuccess()
    }

    private fun getMinScoringOrMinDateQualifications(
        filteredQualifications: List<Qualification>,
        params: RankQualificationsParams
    ): List<Qualification> {
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
        return qualificationsToUpdate
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

        qualificationRepository.update(cpid, ocid, updatedQualifications)
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
                it.responder.id == params.requirementResponse.responder.id
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

                if (params.requirementResponse.responder.name != this.responder.name)
                    return ValidationError.InvalidResponderNameOnCheckDeclaration(
                        actual = params.requirementResponse.responder.name,
                        expected = this.responder.name
                    )
                        .asValidationFailure()
            }

        qualification.requirementResponses
            .find { it.id == params.requirementResponse.id }
            ?.run {
                val receivedResponder = params.requirementResponse.responder
                if (receivedResponder.id != this.responder.id)
                    return ValidationError.ResponderIdMismatchOnCheckDeclaration(actual = receivedResponder.id, expected = this.responder.id)
                        .asValidationFailure()

                if (receivedResponder.name != this.responder.name)
                    return ValidationError.ResponderNameMismatchOnCheckDeclaration(actual = receivedResponder.name, expected = this.responder.name)
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

        qualificationRepository.update(
            cpid = params.cpid, ocid = params.ocid, qualifications = updatedQualifications
        )

        return DoConsiderationResult(qualifications = updatedQualifications.map { updatedQualification ->
            DoConsiderationResult.Qualification(
                id = updatedQualification.id,
                statusDetails = updatedQualification.statusDetails!!
            )
        }).asSuccess()
    }

    override fun setNextForQualification(params: SetNextForQualificationParams): Result<SetNextForQualificationResult?, Fail> {

        val qualifications = qualificationRepository
            .findBy(params.cpid, params.ocid)
            .orForwardFail { fail -> return fail }

        val qualificationSystemMethod = params.tender.otherCriteria.qualificationSystemMethod
        val reductionCriteria = params.tender.otherCriteria.reductionCriteria

        val qualificationForUpdate =  when (qualificationSystemMethod) {
            QualificationSystemMethod.AUTOMATED -> when (reductionCriteria) {
                ReductionCriteria.SCORING -> defineNextforUpdate(qualifications, params.submissions, params.criteria)
                    .orForwardFail { fail -> return fail }
                ReductionCriteria.NONE -> null
            }
            QualificationSystemMethod.MANUAL -> when (reductionCriteria) {
                ReductionCriteria.SCORING,
                ReductionCriteria.NONE -> null
            }
        }

        val result = qualificationForUpdate
            ?.also { qualificationRepository.update(params.cpid, params.ocid, listOf(qualificationForUpdate)) }
            ?.let { SetNextForQualificationResult(listOf(qualificationForUpdate.convertToSetNextForQualification())) }

        return success(result)

    }

    override fun setQualificationPeriodEnd(params: SetQualificationPeriodEndParams): Result<SetQualificationPeriodEndResult, Fail> {
        val storedPeriod = periodRepository.findBy(params.cpid, params.ocid)
            .orForwardFail { fail -> return fail }
            ?: return failure(PeriodNotFoundFor.SetQualificationPeriodEnd(params.cpid, params.ocid))

        val updatedPeriod = storedPeriod.copy(endDate = params.date)

        val result = SetQualificationPeriodEndResult.fromDomain(updatedPeriod)

        periodRepository.saveOrUpdatePeriod(updatedPeriod)

        return success(result)
    }

    override fun doQualification(params: DoQualificationParams): Result<DoQualificationResult, Fail> {

        val cpid = params.cpid
        val ocid = params.ocid

        val qualifications = qualificationRepository
            .findBy(cpid = cpid, ocid = ocid)
            .orForwardFail { fail -> return fail }
            .takeIf { items -> items.isNotEmpty() }
            ?: return ValidationError.QualificationNotFoundFor.DoQualification(
                cpid = cpid,
                ocid = ocid,
                qualificationIds = params.qualifications.map { qualification -> qualification.id }
            ).asFailure()

        val srcQualificationByIds = params.qualifications
            .associateBy { it.id }

        val dstQualificationByIds = qualifications.associateBy { it.id }

        val unknownQualifications = getUnknownElements(
            received = srcQualificationByIds.keys,
            known = dstQualificationByIds.keys
        )
        if (unknownQualifications.isNotEmpty())
            return ValidationError.QualificationNotFoundFor.DoQualification(
                cpid = cpid,
                ocid = ocid,
                qualificationIds = unknownQualifications
            ).asFailure()

        val changedQualifications = mutableListOf<Qualification>()
        val updatedQualifications = qualifications
            .map { qualification ->
                srcQualificationByIds[qualification.id]
                    ?.let { src ->
                        qualification.update(date = params.date, qualification = src)
                            .also {
                                changedQualifications.add(it)
                            }
                    }
                    ?: qualification
            }

        val result = DoQualificationResult(
            qualifications = changedQualifications
                .map {
                    it.convertToDoQualificationResult()
                }
        ).asSuccess<DoQualificationResult, Fail>()

        qualificationRepository.update(cpid, ocid, updatedQualifications)

        return result
    }

    private fun Qualification.update(
        date: LocalDateTime,
        qualification: DoQualificationParams.Qualification
    ): Qualification {

        val documentsById = qualification.documents
            .associateBy { it.id }

        val updatedDocuments = documents
            .map { document ->
                documentsById[document.id]
                    ?.let { document.update(document = it) }
                    ?: document
            }

        val newDocuments = getNewElements(received = documentsById.keys, known = documents.toSetBy { it.id })
            .map { id ->
                buildDocument(document = documentsById.getValue(id))
            }

        return copy(
            date = date, //FR.COM-7.20.9
            internalId = qualification.internalId ?: internalId,  //FR.COM-7.20.2
            description = qualification.description ?: description, //FR.COM-7.20.3
            documents = updatedDocuments + newDocuments,
            statusDetails = qualification.statusDetails  //FR.COM-7.20.1
        )
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

    override fun analyzeQualificationsForInvitation(params: AnalyzeQualificationsForInvitationParams): Result<AnalyzeQualificationsForInvitationResult?, Fail> {
        val qualifications = qualificationRepository.findBy(cpid = params.cpid, ocid = params.ocid)
            .orForwardFail { fail -> return fail }

        val validStates = rulesService.findValidStates(params.country, params.pmd, params.operationType)
            .orForwardFail { fail -> return fail }
            .toSet()

        val qualificationsByStates = qualifications.groupBy { States.State(it.status, it.statusDetails) }
        val suitableQualifications = qualificationsByStates.filter { it.key in validStates }.flatMap { it.value }

        val minimumQuantity = rulesService.findMinimumQualificationQuantity(params.country, params.pmd)
            .orForwardFail { fail -> return fail }
            ?: return ValidationError.RuleNotFound(pmd = params.pmd, country = params.country)
                .asFailure()

        if (suitableQualifications.size < minimumQuantity)
            return null.asSuccess()

        return AnalyzeQualificationsForInvitationResult(
            qualifications = suitableQualifications.map { qualification ->
                AnalyzeQualificationsForInvitationResult.Qualification(
                    id = qualification.id,
                    statusDetails = qualification.statusDetails!!,
                    status = qualification.status,
                    relatedSubmission = qualification.relatedSubmission
                )
            }
        ).asSuccess()
    }

    fun defineStateToUpdate(statusDetails: QualificationStatusDetails?): Option<Pair<QualificationStatus, QualificationStatusDetails>> =
        when (statusDetails) {
            QualificationStatusDetails.ACTIVE ->
                Option.pure(QualificationStatus.ACTIVE to QualificationStatusDetails.BASED_ON_HUMAN_DECISION)
            QualificationStatusDetails.UNSUCCESSFUL->
                Option.pure(QualificationStatus.UNSUCCESSFUL to QualificationStatusDetails.BASED_ON_HUMAN_DECISION)

            QualificationStatusDetails.AWAITING,
            QualificationStatusDetails.CONSIDERATION,
            QualificationStatusDetails.BASED_ON_HUMAN_DECISION,
            null -> Option.none()
        }

    private fun Qualification.Document.update(
        document: DoQualificationParams.Qualification.Document
    ): Qualification.Document = copy(
        title = document.title, //FR.COM-7.20.6
        documentType = document.documentType, //FR.COM-7.20.5
        description = document.description ?: description //FR.COM-7.20.7
    )

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
        criteria: List<RankQualificationsParams.Tender.Criteria>
    ) = if (criteria.map { it.source }.contains(CriteriaSource.PROCURING_ENTITY)) {
        setStatusDetails(
            statusDetails = QualificationStatusDetails.AWAITING,
            qualifications = qualifications
        )
    } else {
        setStatusDetails(
            statusDetails = QualificationStatusDetails.CONSIDERATION,
            qualifications = qualifications
        )
    }

    private fun findMinScoring(qualifications: List<Qualification>) = qualifications.minBy { it.scoring!! }
    private fun hasSameScoring(qualifications: List<Qualification>, scoring: Scoring) = qualifications
        .filter { scoring == it.scoring }
        .count() > 1

    private fun isSameScoring(qualifications: List<SetNextForQualificationWrapper>, scoring: Scoring) = qualifications
        .filter { scoring == it.qualification.scoring }
        .count() > 1

    private fun findMinDate(submissions: List<RankQualificationsParams.Submission>) =
        submissions.minBy { it.date }

    private fun findMinDate(submissions: List<SetNextForQualificationParams.Submission>) =
        submissions.minBy { it.date }

    private fun setStatusDetails(statusDetails: QualificationStatusDetails, qualifications: List<Qualification>) =
        qualifications.map { it.copy(statusDetails = statusDetails) }

    private fun isMatchingDataType(datatype: RequirementDataType, value: RequirementResponseValue) =
        when (value) {
            is RequirementResponseValue.AsString -> datatype == RequirementDataType.STRING
            is RequirementResponseValue.AsBoolean -> datatype == RequirementDataType.BOOLEAN
            is RequirementResponseValue.AsNumber -> datatype == RequirementDataType.NUMBER
            is RequirementResponseValue.AsInteger -> datatype == RequirementDataType.INTEGER
        }

    private fun buildDocument(document: DoQualificationParams.Qualification.Document): Qualification.Document =
        Qualification.Document(
            id = document.id,
            description = document.description,
            title = document.title,
            documentType = document.documentType
        ) //FR.COM-7.20.4

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

    companion object {
        fun isCalculateScoringNeeded(reductionCriteria: ReductionCriteria, qualificationSystemMethod: QualificationSystemMethod): Boolean = when (reductionCriteria) {
            ReductionCriteria.SCORING -> {
                when (qualificationSystemMethod) {
                    QualificationSystemMethod.MANUAL -> false
                    QualificationSystemMethod.AUTOMATED -> true
                }
            }
            ReductionCriteria.NONE -> false
        }

        fun getCoefficients(
            conversions: List<CreateQualificationsParams.Tender.Conversion>,
            requirementResponses: List<CreateQualificationsParams.Submission.RequirementResponse>
        ): List<CoefficientRate> {
            if (requirementResponses.isEmpty()) return emptyList()

            val conversionsRelatesToRequirement: Map<String, CreateQualificationsParams.Tender.Conversion> = conversions
                .filter { it.relatesTo == ConversionRelatesTo.REQUIREMENT }
                .associateBy { it.relatedItem }

            return requirementResponses
                .mapNotNull { requirementResponse ->
                    val id = requirementResponse.requirement.id.toString()
                    conversionsRelatesToRequirement[id]
                        ?.coefficients
                        ?.firstOrNull { coefficient ->
                            isMatchCoefficientValueAndRequirementValue(coefficient.value, requirementResponse.value)
                        }
                        ?.coefficient
                }
        }

        fun calculateScoring(coefficients: List<CoefficientRate>): Scoring {
            val rate: BigDecimal = coefficients.takeIf { it.isNotEmpty() }
                ?.reduce { start, next -> start * next }
                ?.rate
                ?: BigDecimal.ONE
            return Scoring(rate.setScale(3, RoundingMode.HALF_UP))
        }

        fun isMatchCoefficientValueAndRequirementValue(
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
    }
}

class SetNextForQualificationWrapper(
    val qualification: Qualification,
    val dateTime: LocalDateTime
) : Comparable<SetNextForQualificationWrapper> {

    override fun compareTo(other: SetNextForQualificationWrapper): Int {
        val scoringResult = qualification.scoring!!.compareTo(other = other.qualification.scoring!!)
        return if (scoringResult == 0) {
            compareByDates(other.dateTime)
        } else {
            scoringResult
        }
    }

    private fun compareByDates(other: LocalDateTime): Int {
        return dateTime.compareTo(other)
    }
}

fun getQualificationsForProcessing(qualifications: List<Qualification>): List<Qualification> {
    val qualificationsWithoutStatusDetails = mutableListOf<Qualification>()

    qualifications.forEach { qualification ->
        when (qualification.statusDetails) {
            QualificationStatusDetails.AWAITING,
            QualificationStatusDetails.CONSIDERATION -> return emptyList()

            QualificationStatusDetails.ACTIVE,
            QualificationStatusDetails.BASED_ON_HUMAN_DECISION,
            QualificationStatusDetails.UNSUCCESSFUL -> Unit

            null -> qualificationsWithoutStatusDetails.add(qualification)
        }
    }

    return qualificationsWithoutStatusDetails
}

class ValidatedQualification(val value: Qualification)

fun validateQualifications(
    qualifications: List<Qualification>,
    submissions: List<SetNextForQualificationParams.Submission>
): Result<List<ValidatedQualification>, RelatedSubmissionNotEqualOnSetNextForQualification> {
    val qualificationByRelatedSubmission = qualifications.associateBy { it.relatedSubmission }

    return submissions
        .map { (id, _) ->
            qualificationByRelatedSubmission[id]?.let { ValidatedQualification(it) }
                ?: return failure(RelatedSubmissionNotEqualOnSetNextForQualification(submissionId = id))
        }
        .asSuccess()
}

fun getQualificationsForProcessing2(qualifications: List<ValidatedQualification>): List<ValidatedQualification> {
    val qualificationsWithoutStatusDetails = mutableListOf<ValidatedQualification>()

    qualifications.forEach { qualification ->
        when (qualification.value.statusDetails) {
            QualificationStatusDetails.AWAITING,
            QualificationStatusDetails.CONSIDERATION -> return emptyList()

            QualificationStatusDetails.ACTIVE,
            QualificationStatusDetails.UNSUCCESSFUL -> Unit

            null -> qualificationsWithoutStatusDetails.add(qualification)
        }
    }

    return qualificationsWithoutStatusDetails
}

fun sort(qualifications: List<ValidatedQualification>, submissions: List<SetNextForQualificationParams.Submission>): List<ValidatedQualification> {
    val submissionsById = submissions.associateBy { it.id }
    return qualifications
        .map { SetNextForQualificationWrapper2(it, submissionsById.getValue(it.value.relatedSubmission).date) }
        .sorted()
        .map { it.qualification }
}

class SetNextForQualificationWrapper2(
    val qualification: ValidatedQualification,
    val dateTime: LocalDateTime
) : Comparable<SetNextForQualificationWrapper2> {

    override fun compareTo(other: SetNextForQualificationWrapper2): Int {
        val scoringResult = qualification.value.scoring!!.compareTo(other = other.qualification.value.scoring!!)
        return if (scoringResult == 0) {
            compareByDates(other.dateTime)
        } else {
            scoringResult
        }
    }

    private fun compareByDates(other: LocalDateTime): Int {
        return dateTime.compareTo(other)
    }
}

fun defineStatusDetails(criteria: List<SetNextForQualificationParams.Criteria>): QualificationStatusDetails =
    if (criteria.isNullOrEmpty())
        QualificationStatusDetails.CONSIDERATION
    else
        QualificationStatusDetails.AWAITING


fun defineNextforUpdate(qualifications: List<Qualification>, submissions: List<SetNextForQualificationParams.Submission>, criteria: List<SetNextForQualificationParams.Criteria>): Result<Qualification?, Fail> {
    val validatedQualifications = validateQualifications(qualifications, submissions)
        .orForwardFail { fail -> return fail }

    val qualificationsForProcessing = getQualificationsForProcessing2(validatedQualifications)

    return if (qualificationsForProcessing.isNotEmpty())
        qualificationsForProcessing.let { sort(it, submissions) }
            .first()
            .value
            .copy(statusDetails = defineStatusDetails(criteria))
            .asSuccess()
    else
        null.asSuccess()
}