package com.procurement.qualification.infrastructure.handler.set

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class SetQualificationPeriodEndResult(
    @param:JsonProperty("preQualification") @field:JsonProperty("preQualification") val preQualification: PreQualification
) {
    data class PreQualification(
        @param:JsonProperty("period") @field:JsonProperty("period") val period: Period
    ) {
        data class Period(
            @param:JsonProperty("endDate") @field:JsonProperty("endDate") val endDate: LocalDateTime
        )
    }
}
