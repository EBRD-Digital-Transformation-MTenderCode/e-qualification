package com.procurement.qualification.infrastructure.handler.create.declaration


import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.qualification.domain.model.organization.OrganizationId
import com.procurement.qualification.domain.model.person.PersonId
import com.procurement.qualification.domain.model.qualification.QualificationId
import com.procurement.qualification.domain.model.requirement.RequirementId
import com.procurement.qualification.domain.model.requirement.RequirementResponseValue
import com.procurement.qualification.domain.model.requirementresponse.RequirementResponseId

data class DoDeclarationResult(
    @param:JsonProperty("qualifications") @field:JsonProperty("qualifications") val qualifications: List<Qualification>
) {
    data class Qualification(
        @param:JsonProperty("id") @field:JsonProperty("id") val id: QualificationId,
        @param:JsonProperty("requirementResponses") @field:JsonProperty("requirementResponses") val requirementResponses: List<RequirementResponse>
    ) {
        data class RequirementResponse(
            @param:JsonProperty("id") @field:JsonProperty("id") val id: RequirementResponseId,
            @param:JsonProperty("value") @field:JsonProperty("value") val value: RequirementResponseValue,
            @param:JsonProperty("relatedTenderer") @field:JsonProperty("relatedTenderer") val relatedTenderer: RelatedTenderer,
            @param:JsonProperty("requirement") @field:JsonProperty("requirement") val requirement: Requirement,
            @param:JsonProperty("responder") @field:JsonProperty("responder") val responder: Responder
        ) {
            data class RelatedTenderer(
                @param:JsonProperty("id") @field:JsonProperty("id") val id: OrganizationId
            )

            data class Requirement(
                @param:JsonProperty("id") @field:JsonProperty("id") val id: RequirementId
            )

            data class Responder(
                @param:JsonProperty("id") @field:JsonProperty("id") val id: PersonId,
                @param:JsonProperty("name") @field:JsonProperty("name") val name: String
            )
        }
    }
}