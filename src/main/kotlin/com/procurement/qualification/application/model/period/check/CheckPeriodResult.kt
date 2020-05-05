package com.procurement.qualification.application.model.period.check

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class CheckPeriodResult(
    @param:JsonProperty("isPreQualificationPeriodChanged") @field:JsonProperty("isPreQualificationPeriodChanged") val preQualificationPeriodChanged: Boolean,
    @param:JsonProperty("preQualification") @field:JsonProperty("preQualification") val preQualification: PreQualification
) {
    data class PreQualification(
        @param:JsonProperty("period") @field:JsonProperty("period") val period: Period
    ) {
        data class Period(
            @param:JsonProperty("startDate") @field:JsonProperty("startDate") val startDate: LocalDateTime,
            @param:JsonProperty("endDate") @field:JsonProperty("endDate") val endDate: LocalDateTime
        )
    }
}