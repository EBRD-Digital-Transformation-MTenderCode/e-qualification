package com.procurement.qualification.infrastructure.handler.start.qualificationperiod

import com.procurement.qualification.application.model.params.StartQualificationPeriodParams
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.infrastructure.fail.error.DataErrors

fun StartQualificationPeriodRequest.convert(): Result<StartQualificationPeriodParams, DataErrors> =
    StartQualificationPeriodParams.tryCreate(cpid = cpid, date = date, ocid = ocid)
