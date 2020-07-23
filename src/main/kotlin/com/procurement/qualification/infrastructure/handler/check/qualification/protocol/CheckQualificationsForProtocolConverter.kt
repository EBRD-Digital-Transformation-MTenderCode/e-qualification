package com.procurement.qualification.infrastructure.handler.check.qualification.protocol

fun CheckQualificationsForProtocolRequest.convert() = CheckQualificationsForProtocolParams.tryCreate(
    cpid = cpid, ocid = ocid
)
