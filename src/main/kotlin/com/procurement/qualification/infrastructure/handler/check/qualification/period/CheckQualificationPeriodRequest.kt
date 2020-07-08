package com.procurement.qualification.infrastructure.handler.check.qualification.period


import com.fasterxml.jackson.annotation.JsonProperty

data class CheckQualificationPeriodRequest(
    @param:JsonProperty("cpid") @field:JsonProperty("cpid") val cpid: String,
    @param:JsonProperty("ocid") @field:JsonProperty("ocid") val ocid: String,
    @param:JsonProperty("date") @field:JsonProperty("date") val date: String
)