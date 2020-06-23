package com.procurement.qualification.application.service

import com.procurement.qualification.application.model.params.CheckAccessToQualificationParams
import com.procurement.qualification.application.model.params.CheckDeclarationParams
import com.procurement.qualification.application.model.params.CheckQualificationStateParams
import com.procurement.qualification.application.model.params.DoDeclarationParams
import com.procurement.qualification.application.model.params.FindQualificationIdsParams
import com.procurement.qualification.application.model.params.FindRequirementResponseByIdsParams
import com.procurement.qualification.application.repository.QualificationRepository
import com.procurement.qualification.application.repository.QualificationStateRepository
import com.procurement.qualification.domain.enums.RequirementDataType
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.ValidationResult
import com.procurement.qualification.domain.functional.asFailure
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.functional.asValidationFailure
import com.procurement.qualification.domain.model.qualification.Qualification
import com.procurement.qualification.domain.model.qualification.QualificationId
import com.procurement.qualification.domain.model.qualification.RequirementResponseValue
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.fail.error.ValidationError
import com.procurement.qualification.infrastructure.handler.create.declaration.DoDeclarationResult
import com.procurement.qualification.infrastructure.handler.find.requirementresponsebyids.FindRequirementResponseByIdsResult
import com.procurement.qualification.infrastructure.model.entity.QualificationEntity
import org.springframework.stereotype.Service

interface QualificationService {

    fun findQualificationIds(params: FindQualificationIdsParams): Result<List<QualificationId>, Fail>
    fun checkAccessToQualification(params: CheckAccessToQualificationParams): ValidationResult<Fail>
    fun checkQualificationState(params: CheckQualificationStateParams): ValidationResult<Fail>
    fun doDeclaration(params: DoDeclarationParams): Result<DoDeclarationResult, Fail>
    fun checkDeclaration(params: CheckDeclarationParams): ValidationResult<Fail>
    fun findRequirementResponseByIds(params: FindRequirementResponseByIdsParams): Result<FindRequirementResponseByIdsResult, Fail>
}

@Service
class QualificationServiceImpl(
    val qualificationRepository: QualificationRepository,
    val qualificationStateRepository: QualificationStateRepository,
    val transform: Transform
) : QualificationService {

    override fun findQualificationIds(params: FindQualificationIdsParams): Result<List<QualificationId>, Fail> {

        val qualificationEntities = qualificationRepository.findBy(cpid = params.cpid, ocid = params.ocid)
            .orForwardFail { fail -> return fail }

        val qualifications = qualificationEntities
            .map {
                it.convert()
                    .orForwardFail { fail -> return fail }
            }

        if (params.states.isEmpty())
            return qualifications.map { it.id }
                .asSuccess()

        val filteredQualifications = qualifications.filter { qualification ->
            compareStatuses(qualification = qualification, states = params.states)
        }

        return filteredQualifications.map { it.id }
            .asSuccess()
    }

    override fun checkAccessToQualification(params: CheckAccessToQualificationParams): ValidationResult<Fail> {

        val cpid = params.cpid
        val ocid = params.ocid
        val qualificationId = params.qualificationId

        val qualificationEntity = qualificationRepository.findBy(
            cpid = cpid,
            ocid = ocid,
            qualificationId = qualificationId
        )
            .doReturn { fail -> return ValidationResult.error(fail) }
            ?: return ValidationError.QualificationNotFoundByCheckAccessToQualification(
                cpid = cpid,
                ocid = ocid,
                qualificationId = qualificationId
            )
                .asValidationFailure()

        val qualification = qualificationEntity
            .let {
                it.convert()
                    .doReturn { fail -> return ValidationResult.error(fail) }
            }


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

        val qualificationEntity = qualificationRepository.findBy(
            cpid = cpid,
            ocid = ocid,
            qualificationId = qualificationId
        )
            .doReturn { fail -> return ValidationResult.error(fail) }
            ?: return ValidationError.QualificationNotFoundByCheckQualificationState(
                cpid = cpid,
                ocid = ocid,
                qualificationId = qualificationId
            )
                .asValidationFailure()

        val qualification = qualificationEntity
            .let {
                it.convert()
                    .doReturn { fail -> return ValidationResult.error(fail) }
            }

        val stateEntities = qualificationStateRepository.findBy(
            country = params.country,
            operationType = params.operationType,
            pmd = params.pmd
        )
            .doReturn { fail -> return ValidationResult.error(fail) }


        if (stateEntities.isEmpty())
            return ValidationError.QualificationStatesNotFoundOnCheckQualificationState(
                country = params.country,
                operationType = params.operationType,
                pmd = params.pmd
            )
                .asValidationFailure()

        if (stateEntities.any { it.status != qualification.status || it.statusDetails != qualification.statusDetails })
            return ValidationError.QualificationStatesIsInvalidOnCheckQualificationState(qualificationId = qualification.id)
                .asValidationFailure()

        return ValidationResult.ok()
    }

    override fun doDeclaration(params: DoDeclarationParams): Result<DoDeclarationResult, Fail> {
        val cpid = params.cpid
        val ocid = params.ocid

        val qualificationEntities = qualificationRepository.findBy(cpid = cpid, ocid = ocid)
            .orForwardFail { fail -> return fail }

        val dbQualificationsById = qualificationEntities.associateBy { it.qualificationId }

        val filteredQualifications = params.qualifications
            .map {
                dbQualificationsById[it.id]
                    ?: return ValidationError.QualificationNotFoundOnDoDeclaration(
                        cpid = cpid,
                        ocid = ocid,
                        qualificationId = it.id
                    )
                        .asFailure()
            }
            .map {
                it.convert()
                    .orForwardFail { fail -> return fail }
            }

        val filteredDbQualificationsById = filteredQualifications.associateBy { it.id }

        val updatedQualifications = params.qualifications
            .map { rqQualification ->

                val qualification = filteredDbQualificationsById.getValue(rqQualification.id)

                val dbRequirementResponsesById = qualification.requirementResponses
                    .associateBy { it.id }

                val updatedRR = rqQualification
                    .requirementResponses
                    .map { rqRR ->
                        dbRequirementResponsesById[rqRR.id]
                            ?.copy(value = rqRR.value)
                            ?: buildRequirementResponse(rr = rqRR)
                    }
                qualification.copy(requirementResponses = updatedRR)
            }

        val updatedQualificationsEntity = updatedQualifications.map {
            QualificationEntity(
                cpid = cpid,
                ocid = ocid,
                qualificationId = it.id,
                jsonData = transform.trySerialization(value = it)
                    .doReturn { fail ->
                        return Fail.Incident.Database.DatabaseParsing(exception = fail.exception)
                            .asFailure()
                    }
            )
        }

        qualificationRepository.save(entities = updatedQualificationsEntity)
            .orForwardFail { fail -> return fail }

        return updatedQualifications.convertQualificationsToDoDeclarationResult()
            .asSuccess()
    }

    override fun checkDeclaration(params: CheckDeclarationParams): ValidationResult<Fail> {

        val cpid = params.cpid
        val ocid = params.ocid
        val qualificationId = params.qualificationId

        val qualificationEntity = qualificationRepository.findBy(
            cpid = cpid,
            ocid = ocid,
            qualificationId = qualificationId
        )
            .doReturn { fail -> return ValidationResult.error(fail) }
            ?: return ValidationError.QualificationNotFoundOnCheckDeclaration(
                cpid = cpid,
                ocid = ocid,
                qualificationId = qualificationId
            )
                .asValidationFailure()

        val qualification = qualificationEntity.convert()
            .doReturn { fail -> return ValidationResult.error(fail) }

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
                        expected = params.requirementResponse.id,
                        actualId = this.id
                    )
                        .asValidationFailure()
            }

        return ValidationResult.ok()
    }

    override fun findRequirementResponseByIds(params: FindRequirementResponseByIdsParams): Result<FindRequirementResponseByIdsResult, Fail> {

        val cpid = params.cpid
        val ocid = params.ocid
        val qualificationId = params.qualificationId

        val qualificationEntity = qualificationRepository.findBy(
            cpid = cpid,
            ocid = ocid,
            qualificationId = qualificationId
        )
            .orForwardFail { fail -> return fail }
            ?: return ValidationError.QualificationNotFoundOnFindRequirementResponseByIds(cpid, ocid, qualificationId)
                .asFailure()

        val qualification = qualificationEntity.convert()
            .orForwardFail { fail -> return fail }

        val rqRequirementResponsesByIds = params.requirementResponseIds
            .associateBy { it }

        val filteredRequirementResponses = qualification.requirementResponses
            .filter { rqRequirementResponsesByIds[it.id] != null }

        return FindRequirementResponseByIdsResult(
            qualification = FindRequirementResponseByIdsResult.Qualification(
                id = params.qualificationId,
                requirementResponses = filteredRequirementResponses.map { requirementResponse ->
                    requirementResponse.convertToFindRequirementResponseByIdsResultRR()
                }
            )
        )
            .asSuccess()
    }

    private fun QualificationEntity.convert(): Result<Qualification, Fail.Incident.Database.DatabaseParsing> =
        this.let {
            transform.tryDeserialization(value = it.jsonData, target = Qualification::class.java)
                .doReturn { fail ->
                    return Fail.Incident.Database.DatabaseParsing(exception = fail.exception)
                        .asFailure()
                }
        }
            .asSuccess()

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

    private fun buildRequirementResponse(rr: DoDeclarationParams.Qualification.RequirementResponse): Qualification.RequirementResponse =
        Qualification.RequirementResponse(
            id = rr.id,
            value = rr.value,
            relatedTenderer = rr.relatedTenderer
                .let { Qualification.RequirementResponse.RelatedTenderer(id = it.id) },
            requirement = rr.requirement
                .let { Qualification.RequirementResponse.Requirement(id = it.id) },
            responder = rr.responder
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
