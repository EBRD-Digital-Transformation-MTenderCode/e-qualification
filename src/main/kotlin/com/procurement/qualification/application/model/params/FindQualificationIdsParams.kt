package com.procurement.qualification.application.model.params

import com.procurement.qualification.application.model.parseCpid
import com.procurement.qualification.application.model.parseEnum
import com.procurement.qualification.application.model.parseOcid
import com.procurement.qualification.domain.enums.QualificationStatus
import com.procurement.qualification.domain.enums.QualificationStatusDetails
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.infrastructure.fail.error.DataErrors
import com.procurement.qualification.lib.toSetBy

class FindQualificationIdsParams private constructor(
    val cpid: Cpid,
    val ocid: Ocid,
    val states: List<State>
) {

    companion object {
        fun tryCreate(
            cpid: String,
            ocid: String,
            states: List<State>?
        ): Result<FindQualificationIdsParams, DataErrors> {

            val cpidParsed = parseCpid(value = cpid)
                .orForwardFail { fail -> return fail }

            val ocidParsed = parseOcid(value = ocid)
                .orForwardFail { fail -> return fail }

            return FindQualificationIdsParams(cpid = cpidParsed, ocid = ocidParsed, states = states ?: emptyList())
                .asSuccess()
        }
    }

    class State private constructor(val status: QualificationStatus?, val statusDetails: QualificationStatusDetails?) {

        companion object {

            private val validStatuses = QualificationStatus.allowedElements
                .filter {
                    when (it) {
                        QualificationStatus.PENDING,
                        QualificationStatus.ACTIVE,
                        QualificationStatus.UNSUCCESSFUL -> true
                    }
                }
                .toSetBy { it }

            private val validStatusDetails = QualificationStatusDetails.allowedElements
                .filter {
                    when (it) {
                        QualificationStatusDetails.AWAITING,
                        QualificationStatusDetails.CONSIDERATION,
                        QualificationStatusDetails.ACTIVE,
                        QualificationStatusDetails.UNSUCCESSFUL -> true
                        QualificationStatusDetails.BASED_ON_HUMAN_DECISION -> false
                    }
                }
                .toSetBy { it }

            fun tryCreate(status: String?, statusDetails: String?): Result<State, DataErrors> {

                val statusParsed = status
                    ?.let {
                        parseEnum(
                            allowedEnums = validStatuses,
                            target = QualificationStatus,
                            value = it,
                            attributeName = "status"
                        )
                            .orForwardFail { fail -> return fail }
                    }
                val statusDetailsParsed = statusDetails
                    ?.let {
                        parseEnum(
                            allowedEnums = validStatusDetails,
                            target = QualificationStatusDetails,
                            value = it,
                            attributeName = "statusDetails"
                        )
                            .orForwardFail { fail -> return fail }
                    }

                return State(status = statusParsed, statusDetails = statusDetailsParsed)
                    .asSuccess()
            }
        }
    }
}
