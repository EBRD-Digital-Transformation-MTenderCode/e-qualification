package com.procurement.qualification.application.model.params

import com.procurement.qualification.application.model.parseCpid
import com.procurement.qualification.application.model.parseDate
import com.procurement.qualification.application.model.parseEnum
import com.procurement.qualification.application.model.parseOcid
import com.procurement.qualification.application.model.parseSubmissionId
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
import com.procurement.qualification.domain.util.extension.getElementIfOnlyOne
import com.procurement.qualification.infrastructure.fail.error.DataErrors
import com.procurement.qualification.lib.toSetBy
import java.time.LocalDateTime

data class SetNextForQualificationParams(
    val cpid: Cpid,
    val ocid: Ocid,
    val submissions: List<Submission>,
    val tender: Tender,
    val criteria: List<Criteria>
) {

    companion object {
        fun tryCreate(
            cpid: String,
            ocid: String,
            submissions: List<Submission>,
            tender: Tender,
            criteria: List<Criteria>?
        ): Result<SetNextForQualificationParams, DataErrors> {

            val cpidParsed = parseCpid(value = cpid)
                .orForwardFail { fail -> return fail }

            val ocidParsed = parseOcid(value = ocid)
                .orForwardFail { fail -> return fail }

            return SetNextForQualificationParams(
                cpid = cpidParsed,
                ocid = ocidParsed,
                criteria = criteria ?: emptyList(),
                submissions = submissions,
                tender = tender
            )
                .asSuccess()
        }
    }

    data class Submission(
        val id: SubmissionId,
        val date: LocalDateTime
    ) {
        companion object {
            fun tryCreate(
                id: String,
                date: String
            ): Result<Submission, DataErrors> {

                val parsedId = parseSubmissionId(id)
                    .orForwardFail { fail -> return fail }

                val parsedDate = parseDate(value = date,attributeName = "date")
                    .orForwardFail { fail -> return fail }


                return Submission(id = parsedId, date = parsedDate)
                    .asSuccess()
            }
        }
    }

    data class Tender(
        val otherCriteria: OtherCriteria
    ) {

        companion object {
            fun tryCreate(otherCriteria: OtherCriteria): Result<Tender, DataErrors> {
                return Tender(otherCriteria)
                    .asSuccess()
            }
        }

        data class OtherCriteria(
            val qualificationSystemMethod: QualificationSystemMethod,
            val reductionCriteria: ReductionCriteria
        ) {
            companion object {

                private val allowedQualificationSystemMethods = QualificationSystemMethod.allowedElements
                    .filter {
                        when (it) {
                            QualificationSystemMethod.AUTOMATED,
                            QualificationSystemMethod.MANUAL -> true
                        }
                    }
                    .toSet()

                private val allowedReductionCriteria = ReductionCriteria.allowedElements
                    .filter {
                        when (it) {
                            ReductionCriteria.SCORING,
                            ReductionCriteria.NONE -> true
                        }
                    }
                    .toSetBy { it }

                fun tryCreate(
                    qualificationSystemMethods: List<String>,
                    reductionCriteria: String
                ): Result<OtherCriteria, DataErrors> {

                    val oneQualificationSystemMethod = qualificationSystemMethods.getElementIfOnlyOne(name = "qualificationSystemMethods")
                        .orForwardFail { fail -> return fail }

                    val parsedQualificationSystemMethod = parseEnum(
                        value = oneQualificationSystemMethod,
                        attributeName = "qualificationSystemMethods",
                        target = QualificationSystemMethod,
                        allowedEnums = allowedQualificationSystemMethods
                    )
                        .orForwardFail { fail -> return fail }

                    val parsedReductionCriteria = parseEnum(
                        value = reductionCriteria,
                        allowedEnums = allowedReductionCriteria,
                        target = ReductionCriteria,
                        attributeName = "reductionCriteria"
                    )
                        .orForwardFail { fail -> return fail }


                    return OtherCriteria(
                        qualificationSystemMethod = parsedQualificationSystemMethod,
                        reductionCriteria = parsedReductionCriteria
                    )
                        .asSuccess()
                }
            }
        }
    }

    data class Criteria(
        val id: String,
        val title: String,
        val source: CriteriaSource,
        val relatedItem: String,
        val requirementGroups: List<RequirementGroup>,
        val relatesTo: CriteriaRelatesTo?,
        val description: String?
    ) {

        companion object {

            private val allowedSource = CriteriaSource.allowedElements
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
                        CriteriaRelatesTo.TENDERER,
                        CriteriaRelatesTo.QUALIFICATION -> true
                    }
                }
                .toSet()

            fun tryCreate(
                id: String,
                title: String,
                source: String,
                relatedItem: String,
                requirementGroups: List<RequirementGroup>,
                relatesTo: String?,
                description: String?
            ): Result<Criteria, DataErrors> {

                val parsedSource = parseEnum(
                    value = source,
                    attributeName = "source",
                    allowedEnums = allowedSource,
                    target = CriteriaSource
                )
                    .orForwardFail { fail -> return fail }

                val parsedRelatesTo = relatesTo?.let {
                    parseEnum(
                        value = it,
                        target = CriteriaRelatesTo,
                        allowedEnums = allowedRelatesTo,
                        attributeName = "relatesTo"
                    )
                        .orForwardFail { fail -> return fail }
                }
                return Criteria(
                    id = id,
                    description = description,
                    requirementGroups = requirementGroups,
                    title = title,
                    relatedItem = relatedItem,
                    relatesTo = parsedRelatesTo,
                    source = parsedSource
                )
                    .asSuccess()
            }
        }

        data class RequirementGroup(
            val id: String,
            val requirements: List<Requirement>,
            val descriptions: String?
        ) {

            companion object {
                fun tryCreate(
                    id: String,
                    requirements: List<Requirement>,
                    descriptions: String?
                ): Result<RequirementGroup, DataErrors> {
                    return RequirementGroup(id = id, requirements = requirements, descriptions = descriptions)
                        .asSuccess()
                }
            }

            data class Requirement(
                val id: RequirementId,
                val title: String,
                val dataType: RequirementDataType,
                val description: String?
            ) {
                companion object {

                    private val allowedDataType = RequirementDataType.allowedElements
                        .filter {
                            when (it) {
                                RequirementDataType.INTEGER,
                                RequirementDataType.NUMBER,
                                RequirementDataType.BOOLEAN,
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

                        val parsedDataType = parseEnum(
                            value = dataType,
                            attributeName = "dataType",
                            allowedEnums = allowedDataType,
                            target = RequirementDataType
                        )
                            .orForwardFail { fail -> return fail }

                        val parsedRequirementId = RequirementId.parse(id)
                            ?: return DataErrors.Validation.EmptyString(name = id)
                                .asFailure()

                        return Requirement(
                            id = parsedRequirementId,
                            title = title,
                            description = description,
                            dataType = parsedDataType
                        )
                            .asSuccess()
                    }
                }
            }
        }
    }
}
