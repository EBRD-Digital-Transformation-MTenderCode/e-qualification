package com.procurement.qualification.infrastructure.handler.check.declaration


import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.qualification.domain.model.requirement.RequirementResponseValue

data class CheckDeclarationRequest(
    @param:JsonProperty("cpid") @field:JsonProperty("cpid") val cpid: String,
    @param:JsonProperty("ocid") @field:JsonProperty("ocid") val ocid: String,
    @param:JsonProperty("qualificationId") @field:JsonProperty("qualificationId") val qualificationId: String,
    @param:JsonProperty("requirementResponse") @field:JsonProperty("requirementResponse") val requirementResponse: RequirementResponse,
    @param:JsonProperty("criteria") @field:JsonProperty("criteria") val criteria: List<Criteria>
) {
    data class RequirementResponse(
        @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
        @param:JsonProperty("value") @field:JsonProperty("value") val value: RequirementResponseValue,
        @param:JsonProperty("relatedTendererId") @field:JsonProperty("relatedTendererId") val relatedTendererId: String,
        @param:JsonProperty("responderId") @field:JsonProperty("responderId") val responderId: String,
        @param:JsonProperty("requirementId") @field:JsonProperty("requirementId") val requirementId: String
    )

    data class Criteria(
        @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
        @param:JsonProperty("requirementGroups") @field:JsonProperty("requirementGroups") val requirementGroups: List<RequirementGroup>
    ) {
        data class RequirementGroup(
            @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
            @param:JsonProperty("requirements") @field:JsonProperty("requirements") val requirements: List<Requirement>
        ) {
            data class Requirement(
                @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
                @param:JsonProperty("dataType") @field:JsonProperty("dataType") val dataType: String
            )
        }
    }
}
