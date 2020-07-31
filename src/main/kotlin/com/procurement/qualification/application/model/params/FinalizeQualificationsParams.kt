package com.procurement.qualification.application.model.params

import com.procurement.qualification.application.model.parseCpid
import com.procurement.qualification.application.model.parseOcid
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.infrastructure.fail.error.DataErrors

class FinalizeQualificationsParams private constructor(val cpid: Cpid, val ocid: Ocid) {

    companion object {
        fun tryCreate(cpid: String, ocid: String): Result<FinalizeQualificationsParams, DataErrors> {
            val parsedCpid = parseCpid(value = cpid)
                .orForwardFail { fail -> return fail }

            val parsedOcid = parseOcid(value = ocid)
                .orForwardFail { fail -> return fail }

            return FinalizeQualificationsParams(cpid = parsedCpid, ocid = parsedOcid)
                .asSuccess()
        }
    }
}