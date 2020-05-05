package com.procurement.qualification.infrastructure.converter

import com.procurement.qualification.application.model.period.save.SavePeriodContext
import com.procurement.qualification.application.model.period.save.SavePeriodData
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.infrastructure.fail.error.DataErrors
import com.procurement.qualification.infrastructure.web.dto.command.CommandMessage
import com.procurement.qualification.infrastructure.web.dto.command.cpid
import com.procurement.qualification.infrastructure.web.dto.command.ocid
import com.procurement.qualification.infrastructure.web.dto.request.period.SavePeriodRequest

fun SavePeriodRequest.convert() = SavePeriodData.tryCreate(
    period = period.convert()
        .orForwardFail { error -> return error })

fun SavePeriodRequest.Period.convert() =
    SavePeriodData.Period.tryCreate(startDate = startDate, endDate = endDate)

fun CommandMessage.toSavePeriodContext(): Result<SavePeriodContext, DataErrors> =
    SavePeriodContext(
        cpid = cpid.orForwardFail { error -> return error },
        ocid = ocid.orForwardFail { error -> return error }
    ).asSuccess()