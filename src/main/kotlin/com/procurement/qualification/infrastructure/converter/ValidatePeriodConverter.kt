package com.procurement.qualification.infrastructure.converter

import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.model.data.period.validate.ValidatePeriodContext
import com.procurement.qualification.domain.model.data.period.validate.ValidatePeriodData
import com.procurement.qualification.infrastructure.fail.error.DataErrors
import com.procurement.qualification.infrastructure.web.dto.command.CommandMessage
import com.procurement.qualification.infrastructure.web.dto.command.country
import com.procurement.qualification.infrastructure.web.dto.command.pmd
import com.procurement.qualification.infrastructure.web.dto.request.period.ValidatePeriodRequest

fun ValidatePeriodRequest.convert() = ValidatePeriodData.tryCreate(
    period = period.convert()
        .orForwardFail { error -> return error })

fun ValidatePeriodRequest.Period.convert() =
    ValidatePeriodData.Period.tryCreate(startDate = startDate, endDate = endDate)

fun CommandMessage.toValidatePeriodContext(): Result<ValidatePeriodContext, DataErrors> =
    ValidatePeriodContext(
        country = country.orForwardFail { error -> return error },
        pmd = pmd.orForwardFail { error -> return error }
    ).asSuccess()