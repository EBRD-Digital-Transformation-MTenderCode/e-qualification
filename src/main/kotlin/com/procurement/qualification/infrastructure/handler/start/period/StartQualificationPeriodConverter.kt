package com.procurement.qualification.infrastructure.handler.start.period

import com.procurement.qualification.application.model.params.StartQualificationPeriodParams
import com.procurement.qualification.infrastructure.fail.error.DataErrors
import com.procurement.qualification.domain.functional.Result

fun StartQualificationPeriodRequest.convert(): Result<StartQualificationPeriodParams, DataErrors> =
    StartQualificationPeriodParams.tryCreate(cpid = cpid, date = date, ocid = ocid)
