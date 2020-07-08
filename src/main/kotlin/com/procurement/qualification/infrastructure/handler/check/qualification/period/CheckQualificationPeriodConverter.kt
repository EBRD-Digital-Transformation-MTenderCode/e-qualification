package com.procurement.qualification.infrastructure.handler.check.qualification.period

fun CheckQualificationPeriodRequest.convert() = CheckQualificationPeriodParams.tryCreate(
    cpid = cpid, ocid = ocid, date = date
)
