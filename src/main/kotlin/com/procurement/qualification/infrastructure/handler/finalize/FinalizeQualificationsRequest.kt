package com.procurement.qualification.infrastructure.handler.finalize

import com.fasterxml.jackson.annotation.JsonProperty

data class FinalizeQualificationsRequest(
    @param:JsonProperty("cpid") @field:JsonProperty("cpid") val cpid: String,
    @param:JsonProperty("ocid") @field:JsonProperty("ocid") val ocid: String
)
