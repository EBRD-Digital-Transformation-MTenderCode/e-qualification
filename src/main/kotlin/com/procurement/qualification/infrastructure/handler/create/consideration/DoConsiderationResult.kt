package com.procurement.qualification.infrastructure.handler.create.consideration

import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.qualification.domain.enums.QualificationStatusDetails
import com.procurement.qualification.domain.model.qualification.QualificationId

data class DoConsiderationResult(
    @param:JsonProperty("qualifications") @field:JsonProperty("qualifications") val qualifications: List<Qualification>
) {
    data class Qualification(
        @param:JsonProperty("id") @field:JsonProperty("id") val id: QualificationId,
        @param:JsonProperty("statusDetails") @field:JsonProperty("statusDetails") val statusDetails: QualificationStatusDetails
    )
}