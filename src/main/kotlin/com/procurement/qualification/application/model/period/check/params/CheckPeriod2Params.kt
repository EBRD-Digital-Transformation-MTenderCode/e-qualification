package com.procurement.qualification.application.model.period.check.params

import com.procurement.qualification.application.model.parseCpid
import com.procurement.qualification.application.model.parseDate
import com.procurement.qualification.application.model.parseOcid
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.infrastructure.fail.error.DataErrors
import java.time.LocalDateTime

class CheckPeriod2Params private constructor(
    val cpid: Cpid, val ocid: Ocid, val date: LocalDateTime
) {
    companion object {
        private const val DATE_ATTRIBUTE_NAME = "date"

        fun tryCreate(cpid: String, ocid: String, date: String): Result<CheckPeriod2Params, DataErrors> {
            val cpidParsed = parseCpid(cpid)
                .orForwardFail { error -> return error }
            val ocidParsed = parseOcid(ocid)
                .orForwardFail { error -> return error }
            val dateParsed = parseDate(value = date, attributeName = DATE_ATTRIBUTE_NAME)
                .orForwardFail { error -> return error }

            return CheckPeriod2Params(
                cpid = cpidParsed, ocid = ocidParsed, date = dateParsed
            ).asSuccess()
        }
    }
}