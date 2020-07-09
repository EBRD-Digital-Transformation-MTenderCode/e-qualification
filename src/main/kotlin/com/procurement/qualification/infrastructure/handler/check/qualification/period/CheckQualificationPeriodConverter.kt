package com.procurement.qualification.infrastructure.handler.check.qualification.period

import com.procurement.qualification.application.model.params.CheckQualificationPeriodParams

fun CheckQualificationPeriodRequest.convert() = CheckQualificationPeriodParams.tryCreate(
    cpid = cpid, ocid = ocid, date = date
)
