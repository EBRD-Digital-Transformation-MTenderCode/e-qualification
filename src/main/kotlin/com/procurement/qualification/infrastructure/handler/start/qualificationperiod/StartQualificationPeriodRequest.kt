package com.procurement.qualification.infrastructure.handler.start.qualificationperiod

import com.fasterxml.jackson.annotation.JsonProperty

data class StartQualificationPeriodRequest(
    @field:JsonProperty("date") @param:JsonProperty("date") val date: String,
    @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: String,
    @field:JsonProperty("ocid") @param:JsonProperty("ocid") val ocid: String
)
