package com.procurement.qualification.infrastructure.handler.create.qualifications


import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.qualification.domain.model.requirement.RequirementResponseValue
import com.procurement.qualification.domain.model.tender.conversion.coefficient.CoefficientRate
import com.procurement.qualification.domain.model.tender.conversion.coefficient.CoefficientValue

data class CreateQualificationsRequest(
    @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: String,
    @field:JsonProperty("ocid") @param:JsonProperty("ocid") val ocid: String, 
    @field:JsonProperty("date") @param:JsonProperty("date") val date: String,
    @field:JsonProperty("owner") @param:JsonProperty("owner") val owner: String,
    @field:JsonProperty("submissions") @param:JsonProperty("submissions") val submissions: List<Submission>,
    @field:JsonProperty("tender") @param:JsonProperty("tender") val tender: Tender
) {
    data class Submission(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: String,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @field:JsonProperty("requirementResponses") @param:JsonProperty("requirementResponses") val requirementResponses: List<RequirementResponse>?
    ) {
        data class RequirementResponse(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
            @field:JsonProperty("value") @param:JsonProperty("value") val value: RequirementResponseValue,
            @field:JsonProperty("requirement") @param:JsonProperty("requirement") val requirement: Requirement,
            @field:JsonProperty("relatedCandidate") @param:JsonProperty("relatedCandidate") val relatedCandidate: RelatedCandidate
        ) {
            data class Requirement(
                @field:JsonProperty("id") @param:JsonProperty("id") val id: String
            )

            data class RelatedCandidate(
                @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                @field:JsonProperty("name") @param:JsonProperty("name") val name: String
            )
        }
    }

    data class Tender(
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @field:JsonProperty("conversions") @param:JsonProperty("conversions") val conversions: List<Conversion>?,
        @field:JsonProperty("otherCriteria") @param:JsonProperty("otherCriteria") val otherCriteria: OtherCriteria,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @field:JsonProperty("criteria") @param:JsonProperty("criteria") val criteria: List<Criterion>?
    ) {
        data class Conversion(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
            @field:JsonProperty("relatedItem") @param:JsonProperty("relatedItem") val relatedItem: String,
            @field:JsonProperty("relatesTo") @param:JsonProperty("relatesTo") val relatesTo: String,
            @field:JsonProperty("rationale") @param:JsonProperty("rationale") val rationale: String,
            @field:JsonProperty("coefficients") @param:JsonProperty("coefficients") val coefficients: List<Coefficient>,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @field:JsonProperty("description") @param:JsonProperty("description") val description: String?
        ) {
            data class Coefficient(
                @field:JsonProperty("value") @param:JsonProperty("value") val value: CoefficientValue,
                @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                @field:JsonProperty("coefficient") @param:JsonProperty("coefficient") val coefficient: CoefficientRate
            )
        }

        data class OtherCriteria(
            @field:JsonProperty("reductionCriteria") @param:JsonProperty("reductionCriteria") val reductionCriteria: String,
            @field:JsonProperty("qualificationSystemMethods") @param:JsonProperty("qualificationSystemMethods") val qualificationSystemMethods: List<String>
        )

        data class Criterion(
            @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
            @param:JsonProperty("source") @field:JsonProperty("source") val source: String,
            @param:JsonProperty("relatesTo") @field:JsonProperty("relatesTo") val relatesTo: String,
            @param:JsonProperty("requirementGroups") @field:JsonProperty("requirementGroups") val requirementGroups: List<RequirementGroup>,
            @param:JsonProperty("classification") @field:JsonProperty("classification") val classification: Classification
        ) {
            data class RequirementGroup(
                @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
                @param:JsonProperty("requirements") @field:JsonProperty("requirements") val requirements: List<Requirement>
            ) {
                data class Requirement(
                    @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
                    @param:JsonProperty("status") @field:JsonProperty("status") val status: String,
                    @param:JsonProperty("dataType") @field:JsonProperty("dataType") val dataType: String
                )
            }

            data class Classification(
                @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
                @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: String
            )
        }
    }
}
