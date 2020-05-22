package com.procurement.qualification.infrastructure.handler.start.period

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class StartQualificationPeriodResult(
    @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime
)
