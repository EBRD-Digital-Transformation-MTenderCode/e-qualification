package com.procurement.qualification.infrastructure.handler.set.nextforqualification


import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

data class SetNextForQualificationRequest(
    @field:JsonProperty("cpid")  @param:JsonProperty("cpid") val cpid: String,
    @field:JsonProperty("ocid")  @param:JsonProperty("ocid") val ocid: String,
    @field:JsonProperty("submissions")  @param:JsonProperty("submissions") val submissions: List<Submission>,
    @field:JsonProperty("tender")  @param:JsonProperty("tender") val tender: Tender,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @field:JsonProperty("criteria")  @param:JsonProperty("criteria") val criteria: List<Criteria>?
) {
    data class Submission(
        @field:JsonProperty("id")  @param:JsonProperty("id") val id: String,
        @field:JsonProperty("date")  @param:JsonProperty("date") val date: String
    )

    data class Tender(
        @field:JsonProperty("otherCriteria")  @param:JsonProperty("otherCriteria") val otherCriteria: OtherCriteria
    ) {
        data class OtherCriteria(
            @field:JsonProperty("qualificationSystemMethods")  @param:JsonProperty("qualificationSystemMethods") val qualificationSystemMethods: List<String>,
            @field:JsonProperty("reductionCriteria")  @param:JsonProperty("reductionCriteria") val reductionCriteria: String
        )
    }

    data class Criteria(
        @field:JsonProperty("id")  @param:JsonProperty("id") val id: String
    )
}