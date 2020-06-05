package com.procurement.qualification.application.service

import com.procurement.qualification.application.model.params.CreateQualificationsParams
import com.procurement.qualification.application.model.params.FindQualificationIdsParams
import com.procurement.qualification.application.repository.QualificationRepository
import com.procurement.qualification.domain.enums.ConversionRelatesTo
import com.procurement.qualification.domain.enums.QualificationStatus
import com.procurement.qualification.domain.enums.QualificationSystemMethod
import com.procurement.qualification.domain.enums.ReductionCriteria
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asFailure
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.model.measure.Scoring
import com.procurement.qualification.domain.model.qualification.Qualification
import com.procurement.qualification.domain.model.qualification.QualificationId
import com.procurement.qualification.domain.model.requirement.RequirementResponseValue
import com.procurement.qualification.domain.model.tender.conversion.coefficient.CoefficientRate
import com.procurement.qualification.domain.model.tender.conversion.coefficient.CoefficientValue
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.handler.create.qualifications.CreateQualificationsResult
import com.procurement.qualification.infrastructure.model.entity.QualificationEntity
import org.springframework.stereotype.Service
import java.math.BigDecimal

interface QualificationService {

    fun findQualificationIds(params: FindQualificationIdsParams): Result<List<QualificationId>, Fail>

    fun createQualifications(params: CreateQualificationsParams): Result<List<CreateQualificationsResult>, Fail>
}

@Service
class QualificationServiceImpl(
    val qualificationRepository: QualificationRepository,
    val transform: Transform,
    val generationService: GenerationService
) : QualificationService {

    override fun findQualificationIds(params: FindQualificationIdsParams): Result<List<QualificationId>, Fail> {

        val qualificationEntities = qualificationRepository.findBy(cpid = params.cpid, ocid = params.ocid)
            .orForwardFail { fail -> return fail }

        val qualifications = qualificationEntities
            .map {
                transform.tryDeserialization(value = it.jsonData, target = Qualification::class.java)
                    .doOnError { fail ->
                        return Fail.Incident.Database.DatabaseParsing(exception = fail.exception)
                            .asFailure()
                    }
                    .get
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

    override fun createQualifications(params: CreateQualificationsParams): Result<List<CreateQualificationsResult>, Fail> {

        val qualifications = params.submissions
            .map { submission ->
                val scoring: Scoring? = when (params.tender.otherCriteria.reductionCriteria) {
                    ReductionCriteria.SCORING -> {
                        when (params.tender.otherCriteria.qualificationSystemMethod) {
                            QualificationSystemMethod.MANUAL -> null
                            QualificationSystemMethod.AUTOMATED -> {

                                val conversionsRelatesToRequirement = params.tender.conversions
                                    .filter { it.relatesTo == ConversionRelatesTo.REQUIREMENT }
                                    .associateBy { it.relatedItem }

                                Scoring.invoke(
                                    value = submission.requirementResponses
                                        .map { requirementResponse ->
                                            conversionsRelatesToRequirement[requirementResponse.requirement.id]
                                                ?.coefficients
                                                ?.find {
                                                    isMatchCoefficientValueAndRequirementValue(
                                                        coefficientValue = it.value,
                                                        requirementValue = requirementResponse.value
                                                    )
                                                }
                                                ?.coefficient
                                                ?: CoefficientRate(BigDecimal.ONE)
                                        }
                                        .reduce { start, next -> start + next }
                                        .rate
                                )
                            }
                        }
                    }
                    ReductionCriteria.NONE -> null
                }
                Qualification(
                    id = generationService.generateQualificationId(),
                    date = params.date,
                    owner = params.owner,
                    relatedSubmission = submission.id,
                    token = generationService.generateToken(),
                    status = QualificationStatus.PENDING,
                    scoring = scoring
                )

            }

        val qualificationEntities = qualifications.map {
            QualificationEntity(
                cpid = params.cpid,
                ocid = params.ocid,
                id = it.id,
                jsonData = transform.trySerialization(value = it)
                    .orForwardFail { fail -> return fail }
            )
        }

        qualificationRepository.saveAll(qualificationEntities)

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

    private fun compareStatuses(qualification: Qualification, states: List<FindQualificationIdsParams.State>): Boolean {
        return states.any { state ->
            state.status == qualification.status ||
                state.statusDetails == qualification.statusDetails
        }
    }
}
