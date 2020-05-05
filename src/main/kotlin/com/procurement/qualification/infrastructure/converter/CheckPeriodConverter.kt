package com.procurement.qualification.infrastructure.converter

import com.procurement.qualification.application.model.period.check.CheckPeriodContext
import com.procurement.qualification.application.model.period.check.CheckPeriodData
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.infrastructure.fail.error.DataErrors
import com.procurement.qualification.infrastructure.web.dto.command.CommandMessage
import com.procurement.qualification.infrastructure.web.dto.command.cpid
import com.procurement.qualification.infrastructure.web.dto.command.ocid
import com.procurement.qualification.infrastructure.web.dto.request.period.CheckPeriodRequest

fun CheckPeriodRequest.convert() = CheckPeriodData.tryCreate(
    period = period.convert()
        .orForwardFail { error -> return error })

fun CheckPeriodRequest.Period.convert() =
    CheckPeriodData.Period.tryCreate(startDate = startDate, endDate = endDate)

fun CommandMessage.toCheckPeriodContext(): Result<CheckPeriodContext, DataErrors> =
    CheckPeriodContext(
        cpid = cpid.orForwardFail { error -> return error },
        ocid = ocid.orForwardFail { error -> return error }
    ).asSuccess()