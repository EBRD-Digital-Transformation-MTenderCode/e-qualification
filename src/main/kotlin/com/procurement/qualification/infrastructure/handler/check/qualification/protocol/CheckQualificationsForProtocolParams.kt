package com.procurement.qualification.infrastructure.handler.check.qualification.protocol

import com.procurement.qualification.application.model.parseCpid
import com.procurement.qualification.application.model.parseOcid
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.infrastructure.fail.error.DataErrors

class CheckQualificationsForProtocolParams private constructor(
    val cpid: Cpid,
    val ocid: Ocid
) {
    companion object {

        fun tryCreate(
            cpid: String,
            ocid: String
        ): Result<CheckQualificationsForProtocolParams, DataErrors> {
            val cpidParsed = parseCpid(value = cpid)
                .orForwardFail { fail -> return fail }

            val ocidParsed = parseOcid(value = ocid)
                .orForwardFail { fail -> return fail }

            return CheckQualificationsForProtocolParams(
                cpid = cpidParsed, ocid = ocidParsed
            ).asSuccess()
        }
    }
}