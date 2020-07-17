package com.procurement.qualification.application.model.params

import com.procurement.qualification.application.model.parseCpid
import com.procurement.qualification.application.model.parseDate
import com.procurement.qualification.application.model.parseOcid
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.infrastructure.fail.error.DataErrors
import java.time.LocalDateTime

class CheckQualificationPeriodParams private constructor(
    val cpid: Cpid,
    val ocid: Ocid,
    val date: LocalDateTime
) {
    companion object {
        fun tryCreate(
            cpid: String,
            ocid: String,
            date: String
        ): Result<CheckQualificationPeriodParams, DataErrors> {
            val cpidParsed = parseCpid(value = cpid)
                .orForwardFail { fail -> return fail }

            val ocidParsed = parseOcid(value = ocid)
                .orForwardFail { fail -> return fail }

            val dateParsed = parseDate(value = date, attributeName = "date")
                .orForwardFail { fail -> return fail }

            return CheckQualificationPeriodParams(
                cpid = cpidParsed,
                ocid = ocidParsed,
                date = dateParsed
            ).asSuccess()
        }
    }
}