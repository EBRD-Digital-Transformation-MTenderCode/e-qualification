package com.procurement.qualification.infrastructure.handler.check.qualification.state


import com.fasterxml.jackson.annotation.JsonProperty

data class CheckQualificationStateRequest(
    @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: String,
    @field:JsonProperty("ocid") @param:JsonProperty("ocid") val ocid: String,
    @field:JsonProperty("country") @param:JsonProperty("country") val country: String,
    @field:JsonProperty("pmd") @param:JsonProperty("pmd") val pmd: String,
    @field:JsonProperty("operationType") @param:JsonProperty("operationType") val operationType: String,
    @field:JsonProperty("qualificationId") @param:JsonProperty("qualificationId") val qualificationId: String
)
