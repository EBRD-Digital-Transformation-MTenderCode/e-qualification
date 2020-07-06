package com.procurement.qualification.domain.model.qualification

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.qualification.domain.enums.DocumentType
import com.procurement.qualification.domain.enums.QualificationStatus
import com.procurement.qualification.domain.enums.QualificationStatusDetails
import com.procurement.qualification.domain.model.Owner
import com.procurement.qualification.domain.model.Token
import com.procurement.qualification.domain.model.document.DocumentId
import com.procurement.qualification.domain.model.measure.Scoring
import com.procurement.qualification.domain.model.organization.OrganizationId
import com.procurement.qualification.domain.model.person.PersonId
import com.procurement.qualification.domain.model.requirement.RequirementId
import com.procurement.qualification.domain.model.requirement.RequirementResponseValue
import com.procurement.qualification.domain.model.requirementresponse.RequirementResponseId
import com.procurement.qualification.domain.model.submission.SubmissionId
import java.time.LocalDateTime

data class Qualification(
    @param:JsonProperty("id") @field:JsonProperty("id") val id: QualificationId,
    @param:JsonProperty("date") @field:JsonProperty("date") val date: LocalDateTime,
    @param:JsonProperty("status") @field:JsonProperty("status") val status: QualificationStatus,
    @param:JsonProperty("token") @field:JsonProperty("token") val token: Token,
    @param:JsonProperty("owner") @field:JsonProperty("owner") val owner: Owner,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @param:JsonProperty("statusDetails") @field:JsonProperty("statusDetails") val statusDetails: QualificationStatusDetails? = null,
    @param:JsonProperty("relatedSubmission") @field:JsonProperty("relatedSubmission") val relatedSubmission: SubmissionId,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @param:JsonProperty("scoring") @field:JsonProperty("scoring") val scoring: Scoring?,

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @param:JsonProperty("requirementResponses") @field:JsonProperty("requirementResponses") val requirementResponses: List<RequirementResponse> = emptyList(),

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @param:JsonProperty("documents") @field:JsonProperty("documents") val documents: List<Document> = emptyList(),

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @field:JsonProperty("internalId")  @param:JsonProperty("internalId") val internalId: String? = null,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @field:JsonProperty("description")  @param:JsonProperty("description") val description: String? = null
) {
    data class RequirementResponse(
        @param:JsonProperty("id") @field:JsonProperty("id") val id: RequirementResponseId,
        @param:JsonProperty("value") @field:JsonProperty("value") val value: RequirementResponseValue,
        @param:JsonProperty("relatedTenderer") @field:JsonProperty("relatedTenderer") val relatedTenderer: RelatedTenderer,
        @param:JsonProperty("responder") @field:JsonProperty("responder") val responder: Responder,
        @param:JsonProperty("requirement") @field:JsonProperty("requirement") val requirement: Requirement
    ) {
        data class Requirement(
            @param:JsonProperty("id") @field:JsonProperty("id") val id: RequirementId
        )

        data class RelatedTenderer(
            @param:JsonProperty("id") @field:JsonProperty("id") val id: OrganizationId
        )

        data class Responder(
            @param:JsonProperty("id") @field:JsonProperty("id") val id: PersonId,
            @param:JsonProperty("name") @field:JsonProperty("name") val name: String
        )
    }

    data class Document(
        @param:JsonProperty("id") @field:JsonProperty("id") val id: DocumentId,
        @param:JsonProperty("documentType") @field:JsonProperty("documentType") val documentType: DocumentType,
        @param:JsonProperty("title") @field:JsonProperty("title") val title: String,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @param:JsonProperty("description") @field:JsonProperty("description") val description: String?
    )
}
