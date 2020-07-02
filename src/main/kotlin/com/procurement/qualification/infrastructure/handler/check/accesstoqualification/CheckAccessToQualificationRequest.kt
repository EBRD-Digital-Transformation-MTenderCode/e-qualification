package com.procurement.qualification.infrastructure.handler.check.accesstoqualification


import com.fasterxml.jackson.annotation.JsonProperty

data class CheckAccessToQualificationRequest(
    @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: String,
    @field:JsonProperty("ocid") @param:JsonProperty("ocid") val ocid: String,
    @field:JsonProperty("token") @param:JsonProperty("token") val token: String,
    @field:JsonProperty("owner") @param:JsonProperty("owner") val owner: String,
    @field:JsonProperty("qualificationId") @param:JsonProperty("qualificationId") val qualificationId: String
)
