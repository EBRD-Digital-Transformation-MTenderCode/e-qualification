package com.procurement.qualification.infrastructure.handler.get.nextforqualification


import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.qualification.domain.enums.QualificationStatusDetails
import com.procurement.qualification.domain.model.qualification.QualificationId

data class GetNextsForQualificationResult(
    @field:JsonProperty("id") @param:JsonProperty("id") val id: QualificationId,
    @field:JsonProperty("statusDetails") @param:JsonProperty("statusDetails") val statusDetails: QualificationStatusDetails
)
