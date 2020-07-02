package com.procurement.qualification.application.model.params

import com.procurement.qualification.application.model.parseCpid
import com.procurement.qualification.application.model.parseDate
import com.procurement.qualification.application.model.parseEnum
import com.procurement.qualification.application.model.parseOcid
import com.procurement.qualification.domain.enums.CriteriaRelatesTo
import com.procurement.qualification.domain.enums.CriteriaSource
import com.procurement.qualification.domain.enums.QualificationSystemMethod
import com.procurement.qualification.domain.enums.ReductionCriteria
import com.procurement.qualification.domain.enums.RequirementDataType
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asFailure
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.domain.model.requirement.RequirementId
import com.procurement.qualification.domain.model.submission.SubmissionId
import com.procurement.qualification.domain.model.submission.tryCreateSubmissionId
import com.procurement.qualification.domain.util.extension.getElementIfOnlyOne
import com.procurement.qualification.infrastructure.fail.error.DataErrors
import com.procurement.qualification.lib.toSetBy
import java.time.LocalDateTime

class DetermineNextsForQualificationParams private constructor(
    val cpid: Cpid,
    val ocid: Ocid,
    val submissions: List<Submission>,
    val tender: Tender
) {

    companion object {

        fun tryCreate(
            cpid: String,
            ocid: String,
            submissions: List<Submission>,
            tender: Tender
        ): Result<DetermineNextsForQualificationParams, DataErrors> {

            val parsedCpid = parseCpid(value = cpid)
                .orForwardFail { fail -> return fail }

            val parsedOcid = parseOcid(value = ocid)
                .orForwardFail { fail -> return fail }

            return DetermineNextsForQualificationParams(
                cpid = parsedCpid,
                ocid = parsedOcid,
                submissions = submissions,
                tender = tender
            )
                .asSuccess()
        }
    }

    class Tender private constructor(
        val otherCriteria: OtherCriteria,
        val criteria: List<Criteria>?
    ) {

        companion object {
            fun tryCreate(otherCriteria: OtherCriteria, criteria: List<Criteria>?): Result<Tender, DataErrors> =
                Tender(otherCriteria = otherCriteria, criteria = criteria)
                    .asSuccess()
        }

        class OtherCriteria private constructor(
            val qualificationSystemMethod: QualificationSystemMethod,
            val reductionCriteria: ReductionCriteria
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
                        allowedEnums = allowedReductionCriteria,
                        target = ReductionCriteria,
                        attributeName = "reductionCriteria"
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

        class Criteria private constructor(
            val id: String,
            val title: String,
            val requirementGroups: List<RequirementGroup>,
            val source: CriteriaSource,
            val description: String?,
            val relatesTo: CriteriaRelatesTo?,
            val relatedItem: String?
        ) {

            companion object {

                private val allowedSources = CriteriaSource.allowedElements
                    .filter {
                        when (it) {
                            CriteriaSource.PROCURING_ENTITY,
                            CriteriaSource.TENDERER -> true
                        }
                    }
                    .toSet()

                private val allowedRelatesTo = CriteriaRelatesTo.allowedElements
                    .filter {
                        when (it) {
                            CriteriaRelatesTo.AWARD,
                            CriteriaRelatesTo.ITEM,
                            CriteriaRelatesTo.LOT,
                            CriteriaRelatesTo.TENDERER -> true
                        }
                    }
                    .toSet()

                fun tryCreate(
                    id: String,
                    title: String,
                    requirementGroups: List<RequirementGroup>,
                    source: String,
                    description: String?,
                    relatesTo: String?,
                    relatedItem: String?
                ): Result<Criteria, DataErrors> {

                    val parsedCriteriaSource = parseEnum(
                        attributeName = "source",
                        value = source,
                        allowedEnums = allowedSources,
                        target = CriteriaSource
                    )
                        .orForwardFail { fail -> return fail }

                    val parsedCriteriaRelatesTo = relatesTo?.let {
                        parseEnum(
                            attributeName = "relatesTo",
                            value = it,
                            allowedEnums = allowedRelatesTo,
                            target = CriteriaRelatesTo
                        )
                            .orForwardFail { fail -> return fail }
                    }
                    return Criteria(
                        id = id,
                        description = description,
                        source = parsedCriteriaSource,
                        requirementGroups = requirementGroups,
                        relatesTo = parsedCriteriaRelatesTo,
                        relatedItem = relatedItem,
                        title = title
                    )
                        .asSuccess()
                }
            }

            class RequirementGroup private constructor(
                val id: String,
                val description: String?,
                val requirements: List<Requirement>
            ) {
                companion object {
                    fun tryCreate(
                        id: String,
                        description: String?,
                        requirements: List<Requirement>
                    ): Result<RequirementGroup, DataErrors> =
                        RequirementGroup(id = id, description = description, requirements = requirements)
                            .asSuccess()
                }

                class Requirement private constructor(
                    val id: RequirementId,
                    val title: String,
                    val dataType: RequirementDataType,
                    val description: String?
                ) {
                    companion object {

                        private val allowedDataType = RequirementDataType.allowedElements
                            .filter {
                                when (it) {
                                    RequirementDataType.BOOLEAN,
                                    RequirementDataType.INTEGER,
                                    RequirementDataType.NUMBER,
                                    RequirementDataType.STRING -> true
                                }
                            }
                            .toSet()

                        fun tryCreate(
                            id: String,
                            title: String,
                            dataType: String,
                            description: String?
                        ): Result<Requirement, DataErrors> {

                            val parsedRequirementId = RequirementId.parse(id)
                                ?: return DataErrors.Validation.EmptyString(name = id)
                                    .asFailure()


                            val parsedDataType = parseEnum(
                                attributeName = "dataType",
                                value = dataType,
                                allowedEnums = allowedDataType,
                                target = RequirementDataType
                            )
                                .orForwardFail { fail -> return fail }

                            return Requirement(
                                id = parsedRequirementId,
                                description = description,
                                dataType = parsedDataType,
                                title = title
                            )
                                .asSuccess()
                        }
                    }
                }
            }
        }
    }

    class Submission private constructor(
        val id: SubmissionId,
        val date: LocalDateTime
    ) {
        companion object {
            fun tryCreate(id: String, date: String): Result<Submission, DataErrors> {
                val parsedId = tryCreateSubmissionId(value = id)
                    .orForwardFail { fail -> return fail }
                val parsedDate = parseDate(value = date, attributeName = "date")
                    .orForwardFail { fail -> return fail }
                return Submission(id = parsedId, date = parsedDate)
                    .asSuccess()
            }
        }
    }
}
