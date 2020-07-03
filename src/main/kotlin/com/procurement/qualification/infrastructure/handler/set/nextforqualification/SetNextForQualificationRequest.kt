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
        @field:JsonProperty("id")  @param:JsonProperty("id") val id: String,
        @field:JsonProperty("title")  @param:JsonProperty("title") val title: String,
        @field:JsonProperty("source")  @param:JsonProperty("source") val source: String,
        @field:JsonProperty("relatedItem")  @param:JsonProperty("relatedItem") val relatedItem: String,
        @field:JsonProperty("requirementGroups")  @param:JsonProperty("requirementGroups") val requirementGroups: List<RequirementGroup>,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("relatesTo")  @param:JsonProperty("relatesTo") val relatesTo: String?,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("description")  @param:JsonProperty("description") val description: String?
    ) {
        data class RequirementGroup(
            @field:JsonProperty("id")  @param:JsonProperty("id") val id: String,
            @field:JsonProperty("requirements")  @param:JsonProperty("requirements") val requirements: List<Requirement>,
            @JsonInclude(JsonInclude.Include.NON_NULL)
            @field:JsonProperty("descriptions")  @param:JsonProperty("descriptions") val descriptions: String?
        ) {
            data class Requirement(
                @field:JsonProperty("id")  @param:JsonProperty("id") val id: String,
                @field:JsonProperty("title")  @param:JsonProperty("title") val title: String,
                @field:JsonProperty("dataType")  @param:JsonProperty("dataType") val dataType: String,
                @JsonInclude(JsonInclude.Include.NON_NULL)
                @field:JsonProperty("description")  @param:JsonProperty("description") val description: String?
            )
        }
    }
}