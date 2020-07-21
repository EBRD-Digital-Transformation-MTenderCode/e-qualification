package com.procurement.qualification.infrastructure.handler.finalize

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.qualification.domain.enums.QualificationStatus
import com.procurement.qualification.domain.enums.QualificationStatusDetails
import com.procurement.qualification.domain.model.qualification.Qualification
import com.procurement.qualification.domain.model.qualification.QualificationId

data class FinalizeQualificationsResult(
    @param:JsonProperty("qualifications") @field:JsonProperty("qualifications") val qualifications: List<Qualification>
) {
    data class Qualification(
        @param:JsonProperty("id") @field:JsonProperty("id") val id: QualificationId,
        @param:JsonProperty("status") @field:JsonProperty("status") val status: QualificationStatus,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @param:JsonProperty("statusDetails") @field:JsonProperty("statusDetails") val statusDetails: QualificationStatusDetails?
    )
}

fun Qualification.convert(): FinalizeQualificationsResult.Qualification =
    FinalizeQualificationsResult.Qualification(
        id = this.id,
        status = this.status,
        statusDetails = this.statusDetails
    )