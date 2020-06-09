package com.procurement.qualification.infrastructure.handler.get.nextforqualification


import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.qualification.domain.model.measure.Scoring

data class GetNextsForQualificationRequest(
    @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: String,
    @field:JsonProperty("ocid") @param:JsonProperty("ocid") val ocid: String,
    @field:JsonProperty("reductionCriteria") @param:JsonProperty("reductionCriteria") val reductionCriteria: String,
    @field:JsonProperty("qualificationSystemMethods") @param:JsonProperty("qualificationSystemMethods") val qualificationSystemMethods: List<String>,
    @field:JsonProperty("qualifications") @param:JsonProperty("qualifications") val qualifications: List<Qualification>,
    @field:JsonProperty("submissions") @param:JsonProperty("submissions") val submissions: List<Submission>
) {
    data class Qualification(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
        @field:JsonProperty("date") @param:JsonProperty("date") val date: String,
        @field:JsonProperty("relatedSubmission") @param:JsonProperty("relatedSubmission") val relatedSubmission: String,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("scoring") @param:JsonProperty("scoring") val scoring: Scoring?
    )

    data class Submission(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
        @field:JsonProperty("date") @param:JsonProperty("date") val date: String
    )
}
