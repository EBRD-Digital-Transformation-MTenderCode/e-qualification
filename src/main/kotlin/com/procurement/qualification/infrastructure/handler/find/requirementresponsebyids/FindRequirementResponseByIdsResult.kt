package com.procurement.qualification.infrastructure.handler.find.requirementresponsebyids


import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.qualification.domain.model.organization.OrganizationId
import com.procurement.qualification.domain.model.person.PersonId
import com.procurement.qualification.domain.model.qualification.QualificationId
import com.procurement.qualification.domain.model.requirement.RequirementId
import com.procurement.qualification.domain.model.requirement.RequirementResponseValue
import com.procurement.qualification.domain.model.requirementresponse.RequirementResponseId

data class FindRequirementResponseByIdsResult(
    @field:JsonProperty("qualification") @param:JsonProperty("qualification") val qualification: Qualification
) {
    data class Qualification(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: QualificationId,
        @field:JsonProperty("requirementResponses") @param:JsonProperty("requirementResponses") val requirementResponses: List<RequirementResponse>
    ) {
        data class RequirementResponse(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: RequirementResponseId,
            @field:JsonProperty("value") @param:JsonProperty("value") val value: RequirementResponseValue,
            @field:JsonProperty("relatedTenderer") @param:JsonProperty("relatedTenderer") val relatedTenderer: RelatedTenderer,
            @field:JsonProperty("requirement") @param:JsonProperty("requirement") val requirement: Requirement,
            @field:JsonProperty("responder") @param:JsonProperty("responder") val responder: Responder
        ) {
            data class RelatedTenderer(
                @field:JsonProperty("id") @param:JsonProperty("id") val id: OrganizationId
            )

            data class Requirement(
                @field:JsonProperty("id") @param:JsonProperty("id") val id: RequirementId
            )

            data class Responder(
                @field:JsonProperty("id") @param:JsonProperty("id") val id: PersonId,
                @field:JsonProperty("name") @param:JsonProperty("name") val name: String
            )
        }
    }
}
