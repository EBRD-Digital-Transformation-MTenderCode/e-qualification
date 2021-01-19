package com.procurement.qualification.application.model.params

import com.procurement.qualification.application.model.noDuplicatesRule
import com.procurement.qualification.application.model.notEmptyRule
import com.procurement.qualification.application.model.parseCpid
import com.procurement.qualification.application.model.parseCriteriaRelatesTo
import com.procurement.qualification.application.model.parseDataType
import com.procurement.qualification.application.model.parseDate
import com.procurement.qualification.application.model.parseEnum
import com.procurement.qualification.application.model.parseOcid
import com.procurement.qualification.application.model.parseOwner
import com.procurement.qualification.application.model.parseRequirementId
import com.procurement.qualification.application.model.parseRequirementStatus
import com.procurement.qualification.application.model.parseSubmissionId
import com.procurement.qualification.domain.enums.ConversionRelatesTo
import com.procurement.qualification.domain.enums.CriteriaRelatesTo
import com.procurement.qualification.domain.enums.QualificationSystemMethod
import com.procurement.qualification.domain.enums.ReductionCriteria
import com.procurement.qualification.domain.enums.RequirementDataType
import com.procurement.qualification.domain.enums.RequirementStatus
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asFailure
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.functional.validate
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.domain.model.Owner
import com.procurement.qualification.domain.model.requirement.RequirementId
import com.procurement.qualification.domain.model.requirement.RequirementResponseValue
import com.procurement.qualification.domain.model.requirementresponse.RequirementResponseId
import com.procurement.qualification.domain.model.submission.SubmissionId
import com.procurement.qualification.domain.model.tender.conversion.ConversionId
import com.procurement.qualification.domain.model.tender.conversion.coefficient.CoefficientId
import com.procurement.qualification.domain.model.tender.conversion.coefficient.CoefficientRate
import com.procurement.qualification.domain.model.tender.conversion.coefficient.CoefficientValue
import com.procurement.qualification.domain.model.tender.conversion.tryCreateConversionId
import com.procurement.qualification.domain.util.extension.getElementIfOnlyOne
import com.procurement.qualification.infrastructure.fail.error.DataErrors
import com.procurement.qualification.lib.toSetBy
import java.time.LocalDateTime

class CreateQualificationsParams private constructor(
    val cpid: Cpid,
    val ocid: Ocid,
    val date: LocalDateTime,
    val owner: Owner,
    val submissions: List<Submission>,
    val tender: Tender
) {

    companion object {
        fun tryCreate(
            cpid: String,
            ocid: String,
            date: String,
            owner: String,
            submissions: List<Submission>,
            tender: Tender
        ): Result<CreateQualificationsParams, DataErrors> {

            val cpidParsed = parseCpid(value = cpid)
                .orForwardFail { fail -> return fail }

            val ocidParsed = parseOcid(value = ocid)
                .orForwardFail { fail -> return fail }

            val parsedDate = parseDate(value = date, attributeName = "date")
                .orForwardFail { fail -> return fail }

            val parsedOwner = parseOwner(value = owner)
                .orForwardFail { fail -> return fail }

            return CreateQualificationsParams(
                cpid = cpidParsed,
                ocid = ocidParsed,
                date = parsedDate,
                owner = parsedOwner,
                submissions = submissions,
                tender = tender
            )
                .asSuccess()
        }
    }

    class Submission private constructor(
        val id: SubmissionId,
        val requirementResponses: List<RequirementResponse>
    ) {

        companion object {
            fun tryCreate(
                id: String,
                requirementResponses: List<RequirementResponse>
            ): Result<Submission, DataErrors> {

                val parsedId = parseSubmissionId(value = id)
                    .orForwardFail { fail -> return fail }

                return Submission(id = parsedId, requirementResponses = requirementResponses)
                    .asSuccess()
            }
        }

        class RequirementResponse private constructor(
            val id: RequirementResponseId,
            val value: RequirementResponseValue,
            val requirement: Requirement,
            val relatedCandidate: RelatedCandidate
        ) {

            companion object {
                fun tryCreate(
                    id: String,
                    value: RequirementResponseValue,
                    requirement: Requirement,
                    relatedCandidate: RelatedCandidate
                ): Result<RequirementResponse, DataErrors> {

                    val parsedId = RequirementResponseId.tryCreate(text = id)
                        .orForwardFail { fail -> return fail }

                    return RequirementResponse(
                        id = parsedId,
                        value = value,
                        requirement = requirement,
                        relatedCandidate = relatedCandidate
                    )
                        .asSuccess()
                }
            }

            class Requirement private constructor(val id: RequirementId) {
                companion object {
                    fun tryCreate(id: String): Result<Requirement, DataErrors> {

                        val parsedRequirementId = RequirementId.parse(id)
                            ?: return DataErrors.Validation.EmptyString(name = id)
                                .asFailure()

                        return Requirement(id = parsedRequirementId)
                            .asSuccess()
                    }
                }
            }

            class RelatedCandidate private constructor(
                val id: String,
                val name: String
            ) {
                companion object {
                    fun tryCreate(id: String, name: String): Result<RelatedCandidate, DataErrors> {
                        return RelatedCandidate(id = id, name = name)
                            .asSuccess()
                    }
                }
            }
        }
    }

    class Tender private constructor(
        val conversions: List<Conversion>,
        val otherCriteria: OtherCriteria,
        val criteria: List<Criterion>
    ) {

        companion object {
            fun tryCreate(
                conversions: List<Conversion>?,
                otherCriteria: OtherCriteria,
                criteria: List<Criterion>?
            ): Result<Tender, DataErrors> {
                criteria.validate(notEmptyRule("tender.criteria"))
                    .orForwardFail { return it }
                    .validate(noDuplicatesRule("tender.criteria") { it.id })
                    .orForwardFail { return it }

                return Tender(
                    conversions = conversions ?: emptyList(),
                    otherCriteria = otherCriteria,
                    criteria = criteria ?: emptyList()
                )
                    .asSuccess()
            }
        }

        class Conversion private constructor(
            val id: ConversionId,
            val relatedItem: String,
            val relatesTo: ConversionRelatesTo,
            val rationale: String,
            val coefficients: List<Coefficient>,
            val description: String?
        ) {

            companion object {

                private val allowedRelatesTo = ConversionRelatesTo.allowedElements
                    .filter {
                        when (it) {
                            ConversionRelatesTo.REQUIREMENT -> true
                        }
                    }
                    .toSetBy { it }

                fun tryCreate(
                    id: String,
                    relatedItem: String,
                    relatesTo: String,
                    rationale: String,
                    coefficients: List<Coefficient>,
                    description: String?
                ): Result<Conversion, DataErrors> {
                    val parsedId = tryCreateConversionId(value = id)
                        .orForwardFail { fail -> return fail }

                    val parsedRelatesTo = parseEnum(
                        value = relatesTo,
                        attributeName = "relatesTo",
                        target = ConversionRelatesTo,
                        allowedEnums = allowedRelatesTo
                    )
                        .orForwardFail { fail -> return fail }

                    return Conversion(
                        id = parsedId,
                        relatedItem = relatedItem,
                        rationale = rationale,
                        coefficients = coefficients,
                        description = description,
                        relatesTo = parsedRelatesTo
                    )
                        .asSuccess()
                }
            }

            class Coefficient private constructor(
                val value: CoefficientValue,
                val id: CoefficientId,
                val coefficient: CoefficientRate
            ) {
                companion object {
                    fun tryCreate(
                        value: CoefficientValue,
                        id: String,
                        coefficient: CoefficientRate
                    ): Result<Coefficient, DataErrors> {

                        return Coefficient(id = id, value = value, coefficient = coefficient)
                            .asSuccess()
                    }
                }
            }
        }

        class OtherCriteria private constructor(
            val reductionCriteria: ReductionCriteria,
            val qualificationSystemMethod: QualificationSystemMethod
        ) {
            companion object {

                private val allowedReductionCriteria = ReductionCriteria.allowedElements
                    .filter {
                        when (it) {
                            ReductionCriteria.SCORING,
                            ReductionCriteria.NONE -> true
                        }
                    }
                    .toSetBy { it }

                private val allowedQualificationSystemMethods = QualificationSystemMethod.allowedElements
                    .filter {
                        when (it) {
                            QualificationSystemMethod.AUTOMATED,
                            QualificationSystemMethod.MANUAL -> true
                        }
                    }
                    .toSetBy { it }

                fun tryCreate(
                    reductionCriteria: String,
                    qualificationSystemMethods: List<String>
                ): Result<OtherCriteria, DataErrors> {

                    val parsedReductionCriteria = parseEnum(
                        value = reductionCriteria,
                        attributeName = "reductionCriteria",
                        target = ReductionCriteria,
                        allowedEnums = allowedReductionCriteria
                    )
                        .orForwardFail { fail -> return fail }

                    val oneQualificationSystemMethod = qualificationSystemMethods.getElementIfOnlyOne(name = "qualificationSystemMethods")
                        .orForwardFail { fail -> return fail }

                    val parsedQualificationSystemMethod = parseEnum(
                        value = oneQualificationSystemMethod,
                        attributeName = "qualificationSystemMethods",
                        target = QualificationSystemMethod,
                        allowedEnums = allowedQualificationSystemMethods
                    )
                        .orForwardFail { fail -> return fail }

                    return OtherCriteria(
                        reductionCriteria = parsedReductionCriteria,
                        qualificationSystemMethod = parsedQualificationSystemMethod
                    )
                        .asSuccess()
                }
            }
        }

        class Criterion private constructor(
            val id: String,
            val source: String,
            val relatesTo: CriteriaRelatesTo,
            val requirementGroups: List<RequirementGroup>,
            val classification: Classification
        ) {
            companion object {
                val allowedRelatesTo = CriteriaRelatesTo.allowedElements.toSet()

                fun tryCreate(
                    id: String,
                    source: String,
                    relatesTo: String,
                    requirementGroups: List<RequirementGroup>,
                    classification: Classification
                ): Result<Criterion, DataErrors> {
                    requirementGroups.validate(notEmptyRule("tender.criteria.requirementGroups"))
                        .orForwardFail { return it }
                        .validate(noDuplicatesRule("tender.criteria.requirementGroups") { it.id })
                        .orForwardFail { return it }

                    val relatesToParsed = parseCriteriaRelatesTo(
                        relatesTo, allowedRelatesTo, "tender.criteria.relatesTo"
                    ).orForwardFail { return it }

                    return Criterion(
                        id = id,
                        source = source,
                        relatesTo = relatesToParsed,
                        requirementGroups = requirementGroups,
                        classification = classification
                    ).asSuccess()
                }
            }

            class RequirementGroup private constructor(
                val id: String,
                val requirements: List<Requirement>
            ) {
                companion object {
                    fun tryCreate(
                        id: String,
                        requirements: List<Requirement>
                    ): Result<RequirementGroup, DataErrors> {
                        requirements.validate(notEmptyRule("tender.criteria.requirementGroups.requirements"))
                            .orForwardFail { return it }
                            .validate(noDuplicatesRule("tender.criteria.requirementGroups.requirements") { it.id })
                            .orForwardFail { return it }

                        return RequirementGroup(
                            id = id,
                            requirements = requirements
                        ).asSuccess()
                    }
                }

                class Requirement private constructor(
                    val id: RequirementId,
                    val status: RequirementStatus,
                    val dataType: RequirementDataType
                ) {

                    companion object {
                        val ALLOWED_REQUIREMENT_STATUSES = RequirementStatus.allowedElements
                            .filter {
                                when (it) {
                                    RequirementStatus.ACTIVE -> true
                                }
                            }.toSet()

                        val ALLOWED_REQUIREMENT_DATA_TYPE = RequirementDataType.allowedElements.toSet()

                        const val REQUIREMENTS_ID_ATTRIBUTE = "tender.criteria.requirementGroups.requirements.id"
                        const val REQUIREMENTS_STATUS_ATTRIBUTE = "tender.criteria.requirementGroups.requirements.status"
                        const val REQUIREMENTS_DATA_TYPE_ATTRIBUTE = "tender.criteria.requirementGroups.requirements.dataType"

                        fun tryCreate(
                            id: String,
                            status: String,
                            dataType: String
                        ): Result<Requirement, DataErrors> {
                            val requirementId = parseRequirementId(id, REQUIREMENTS_ID_ATTRIBUTE)
                                .orForwardFail { return it }

                            val parsedStatus = parseRequirementStatus(
                                status, ALLOWED_REQUIREMENT_STATUSES, REQUIREMENTS_STATUS_ATTRIBUTE
                            ).orForwardFail { return it }

                            val parseDataType = parseDataType(
                                dataType, ALLOWED_REQUIREMENT_DATA_TYPE, REQUIREMENTS_DATA_TYPE_ATTRIBUTE
                            ).orForwardFail { return it }

                            return Requirement(
                                id = requirementId,
                                status = parsedStatus,
                                dataType = parseDataType
                            ).asSuccess()
                        }
                    }
                }
            }

            data class Classification(
                val id: String,
                val scheme: String
            )
        }
    }
}
