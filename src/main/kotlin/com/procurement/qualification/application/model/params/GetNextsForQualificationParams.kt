package com.procurement.qualification.application.model.params

import com.procurement.qualification.application.model.parseCpid
import com.procurement.qualification.application.model.parseDate
import com.procurement.qualification.application.model.parseEnum
import com.procurement.qualification.application.model.parseOcid
import com.procurement.qualification.application.model.parseQualificationId
import com.procurement.qualification.domain.enums.QualificationSystemMethod
import com.procurement.qualification.domain.enums.ReductionCriteria
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.domain.model.measure.Scoring
import com.procurement.qualification.domain.model.qualification.QualificationId
import com.procurement.qualification.domain.model.submission.SubmissionId
import com.procurement.qualification.domain.model.submission.tryCreateSubmissionId
import com.procurement.qualification.domain.util.extension.getElementIfOnlyOne
import com.procurement.qualification.infrastructure.fail.error.DataErrors
import com.procurement.qualification.lib.toSetBy
import java.time.LocalDateTime

class GetNextsForQualificationParams private constructor(
    val cpid: Cpid,
    val ocid: Ocid,
    val reductionCriteria: ReductionCriteria,
    val qualificationSystemMethod: QualificationSystemMethod,
    val qualifications: List<Qualification>,
    val submissions: List<Submission>
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
            cpid: String,
            ocid: String,
            reductionCriteria: String,
            qualificationSystemMethods: List<String>,
            qualifications: List<Qualification>,
            submissions: List<Submission>
        ): Result<GetNextsForQualificationParams, DataErrors> {

            val parsedCpid = parseCpid(value = cpid)
                .orForwardFail { fail -> return fail }

            val parsedOcid = parseOcid(value = ocid)
                .orForwardFail { fail -> return fail }

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


            return GetNextsForQualificationParams(
                cpid = parsedCpid,
                ocid = parsedOcid,
                qualificationSystemMethod = parsedQualificationSystemMethod,
                submissions = submissions,
                reductionCriteria = parsedReductionCriteria,
                qualifications = qualifications
            )
                .asSuccess()
        }
    }

    class Qualification private constructor(
        val id: QualificationId,
        val date: LocalDateTime,
        val relatedSubmission: SubmissionId,
        val scoring: Scoring?
    ) {
        companion object {
            fun tryCreate(
                id: String,
                date: String,
                relatedSubmission: String,
                scoring: Scoring?
            ): Result<Qualification, DataErrors> {

                val parsedId = parseQualificationId(value = id)
                    .orForwardFail { fail -> return fail }
                val parsedDate = parseDate(value = date, attributeName = "date")
                    .orForwardFail { fail -> return fail }
                val parsedRelatedSubmission = tryCreateSubmissionId(value = relatedSubmission)
                    .orForwardFail { fail -> return fail }
                return Qualification(
                    id = parsedId,
                    date = parsedDate,
                    relatedSubmission = parsedRelatedSubmission,
                    scoring = scoring
                )
                    .asSuccess()
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
