package com.procurement.qualification.infrastructure.handler.check.qualification.protocol

import com.fasterxml.jackson.annotation.JsonProperty

data class CheckQualificationsForProtocolRequest(
    @param:JsonProperty("cpid") @field:JsonProperty("cpid") val cpid: String,
    @param:JsonProperty("ocid") @field:JsonProperty("ocid") val ocid: String
)