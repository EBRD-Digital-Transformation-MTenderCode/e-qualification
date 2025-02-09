package com.procurement.qualification.application.model.params

import com.procurement.qualification.application.model.parseCpid
import com.procurement.qualification.application.model.parseDate
import com.procurement.qualification.application.model.parseEnum
import com.procurement.qualification.application.model.parseOcid
import com.procurement.qualification.application.model.parseSubmissionId
import com.procurement.qualification.domain.enums.CriteriaSource
import com.procurement.qualification.domain.enums.QualificationSystemMethod
import com.procurement.qualification.domain.enums.ReductionCriteria
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.domain.model.submission.SubmissionId
import com.procurement.qualification.domain.util.extension.getElementIfOnlyOne
import com.procurement.qualification.infrastructure.fail.error.DataErrors
import com.procurement.qualification.lib.toSetBy
import java.time.LocalDateTime

class RankQualificationsParams private constructor(
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
        ): Result<RankQualificationsParams, DataErrors> {

            val parsedCpid = parseCpid(value = cpid)
                .orForwardFail { fail -> return fail }

            val parsedOcid = parseOcid(value = ocid)
                .orForwardFail { fail -> return fail }

            return RankQualificationsParams(
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
        val criteria: List<Criteria>
    ) {

        companion object {
            fun tryCreate(otherCriteria: OtherCriteria, criteria: List<Criteria>?): Result<Tender, DataErrors> =
                Tender(otherCriteria = otherCriteria, criteria = criteria ?: emptyList())
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
            val source: CriteriaSource
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

                fun tryCreate(
                    id: String,
                    source: String
                ): Result<Criteria, DataErrors> {

                    val parsedCriteriaSource = parseEnum(
                        attributeName = "source",
                        value = source,
                        allowedEnums = allowedSources,
                        target = CriteriaSource
                    )
                        .orForwardFail { fail -> return fail }

                    return Criteria(
                        id = id,
                        source = parsedCriteriaSource
                    )
                        .asSuccess()
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
                val parsedId = parseSubmissionId(value = id)
                    .orForwardFail { fail -> return fail }
                val parsedDate = parseDate(value = date, attributeName = "date")
                    .orForwardFail { fail -> return fail }
                return Submission(id = parsedId, date = parsedDate)
                    .asSuccess()
            }
        }
    }
}
