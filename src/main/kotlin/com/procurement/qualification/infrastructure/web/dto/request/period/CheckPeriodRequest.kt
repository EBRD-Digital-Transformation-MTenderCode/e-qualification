package com.procurement.qualification.infrastructure.web.dto.request.period

import com.fasterxml.jackson.annotation.JsonProperty

data class CheckPeriodRequest(
    @param:JsonProperty("period") @field:JsonProperty("period") val period: Period
) {
    data class Period(
        @param:JsonProperty("startDate") @field:JsonProperty("startDate") val startDate: String,
        @param:JsonProperty("endDate") @field:JsonProperty("endDate") val endDate: String
    )
}