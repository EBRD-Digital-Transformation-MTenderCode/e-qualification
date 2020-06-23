package com.procurement.qualification.infrastructure.handler.determine.nextforqualification

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

data class DetermineNextsForQualificationRequest(
    @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: String,
    @field:JsonProperty("ocid") @param:JsonProperty("ocid") val ocid: String,
    @field:JsonProperty("submissions") @param:JsonProperty("submissions") val submissions: List<Submission>,
    @param:JsonProperty("tender") @field:JsonProperty("tender") val tender: Tender
) {

    data class Tender(
        @param:JsonProperty("otherCriteria") @field:JsonProperty("otherCriteria") val otherCriteria: OtherCriteria,
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @param:JsonProperty("criteria") @field:JsonProperty("criteria") val criteria: List<Criteria>?
    ) {
        data class OtherCriteria(
            @param:JsonProperty("qualificationSystemMethods") @field:JsonProperty("qualificationSystemMethods") val qualificationSystemMethods: List<String>,
            @param:JsonProperty("reductionCriteria") @field:JsonProperty("reductionCriteria") val reductionCriteria: String
        )

        data class Criteria(
            @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
            @param:JsonProperty("title") @field:JsonProperty("title") val title: String,
            @param:JsonProperty("requirementGroups") @field:JsonProperty("requirementGroups") val requirementGroups: List<RequirementGroup>,
            @param:JsonProperty("source") @field:JsonProperty("source") val source: String,
            @JsonInclude(JsonInclude.Include.NON_NULL)
            @param:JsonProperty("description") @field:JsonProperty("description") val description: String?,
            @JsonInclude(JsonInclude.Include.NON_NULL)
            @param:JsonProperty("relatesTo") @field:JsonProperty("relatesTo") val relatesTo: String?,
            @JsonInclude(JsonInclude.Include.NON_NULL)
            @param:JsonProperty("relatedItem") @field:JsonProperty("relatedItem") val relatedItem: String?
        ) {
            data class RequirementGroup(
                @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
                @param:JsonProperty("requirements") @field:JsonProperty("requirements") val requirements: List<Requirement>,
                @JsonInclude(JsonInclude.Include.NON_NULL)
                @param:JsonProperty("description") @field:JsonProperty("description") val description: String?
            ) {
                data class Requirement(
                    @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
                    @param:JsonProperty("title") @field:JsonProperty("title") val title: String,
                    @param:JsonProperty("dataType") @field:JsonProperty("dataType") val dataType: String,
                    @JsonInclude(JsonInclude.Include.NON_NULL)
                    @param:JsonProperty("description") @field:JsonProperty("description") val description: String?
                )
            }
        }
    }

    data class Submission(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
        @field:JsonProperty("date") @param:JsonProperty("date") val date: String
    )
}
