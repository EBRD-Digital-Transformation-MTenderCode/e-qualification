package com.procurement.qualification.domain.model.data.period.validate

import com.procurement.qualification.application.model.parseDate
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.infrastructure.fail.error.DataErrors
import java.time.LocalDateTime

class ValidatePeriodData private constructor(
    val period: Period
) {
    companion object {
        fun tryCreate(period: Period) = ValidatePeriodData(period).asSuccess<ValidatePeriodData, DataErrors>()
    }

    class Period private constructor(
        val startDate: LocalDateTime,
        val endDate: LocalDateTime
    ) {
        companion object {
            private const val START_DATE_ATTRIBUTE_NAME = "startDate"
            private const val END_DATE_ATTRIBUTE_NAME = "endDate"

            fun tryCreate(startDate: String, endDate: String): Result<Period, DataErrors> {
                val startDateParsed = parseDate(startDate, START_DATE_ATTRIBUTE_NAME)
                    .orForwardFail { error -> return error }

                val endDateParsed = parseDate(endDate, END_DATE_ATTRIBUTE_NAME)
                    .orForwardFail { error -> return error }

                return Period(startDate = startDateParsed, endDate = endDateParsed).asSuccess()
            }
        }
    }
}