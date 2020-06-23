package com.procurement.qualification.infrastructure.handler.determine.nextforqualification


import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.qualification.domain.enums.QualificationStatusDetails
import com.procurement.qualification.domain.model.qualification.QualificationId

data class DetermineNextsForQualificationResult(
    @field:JsonProperty("id") @param:JsonProperty("id") val id: QualificationId,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @field:JsonProperty("statusDetails") @param:JsonProperty("statusDetails") val statusDetails: QualificationStatusDetails?
)
