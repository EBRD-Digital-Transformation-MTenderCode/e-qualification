package com.procurement.qualification.application.model.params

import com.procurement.qualification.application.model.parseCpid
import com.procurement.qualification.application.model.parseDate
import com.procurement.qualification.application.model.parseEnum
import com.procurement.qualification.application.model.parseOcid
import com.procurement.qualification.application.model.parseOwner
import com.procurement.qualification.domain.enums.ConversionRelatesTo
import com.procurement.qualification.domain.enums.QualificationSystemMethod
import com.procurement.qualification.domain.enums.ReductionCriteria
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asFailure
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.domain.model.Owner
import com.procurement.qualification.domain.model.requirement.RequirementId
import com.procurement.qualification.domain.model.requirement.RequirementResponseValue
import com.procurement.qualification.domain.model.requirementresponse.RequirementResponseId
import com.procurement.qualification.domain.model.submission.SubmissionId
import com.procurement.qualification.domain.model.submission.tryCreateSubmissionId
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

                val parsedId = tryCreateSubmissionId(value = id)
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
        val otherCriteria: OtherCriteria
    ) {

        companion object {
            fun tryCreate(conversions: List<Conversion>?, otherCriteria: OtherCriteria): Result<Tender, DataErrors> {
                return Tender(conversions = conversions ?: emptyList(), otherCriteria = otherCriteria)
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
    }
}
