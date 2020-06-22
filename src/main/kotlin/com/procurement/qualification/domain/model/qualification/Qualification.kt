package com.procurement.qualification.domain.model.qualification

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.qualification.domain.enums.DocumentType
import com.procurement.qualification.domain.enums.QualificationStatus
import com.procurement.qualification.domain.enums.QualificationStatusDetails
import com.procurement.qualification.domain.model.Owner
import com.procurement.qualification.domain.model.Token
import com.procurement.qualification.domain.model.measure.Scoring
import com.procurement.qualification.domain.model.submission.SubmissionId
import java.time.LocalDateTime

data class Qualification(
    @param:JsonProperty("id") @field:JsonProperty("id") val id: QualificationId,
    @param:JsonProperty("date") @field:JsonProperty("date") val date: LocalDateTime,
    @param:JsonProperty("status") @field:JsonProperty("status") val status: QualificationStatus,
    @param:JsonProperty("token") @field:JsonProperty("token") val token: Token,
    @param:JsonProperty("owner") @field:JsonProperty("owner") val owner: Owner,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @param:JsonProperty("statusDetails") @field:JsonProperty("statusDetails") val statusDetails: QualificationStatusDetails?,
    @param:JsonProperty("relatedSubmission") @field:JsonProperty("relatedSubmission") val relatedSubmission: SubmissionId,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @param:JsonProperty("scoring") @field:JsonProperty("scoring") val scoring: Scoring?,

    @param:JsonProperty("scoring") @field:JsonProperty("scoring") val requirementResponses: List<RequirementResponse>
) {
    data class RequirementResponse(
        @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
        @param:JsonProperty("value") @field:JsonProperty("value") val value: RequirementResponseValue,
        @param:JsonProperty("relatedTenderer") @field:JsonProperty("relatedTenderer") val relatedTenderer: RelatedTenderer,
        @param:JsonProperty("responder") @field:JsonProperty("responder") val responder: Responder,
        @param:JsonProperty("requirement") @field:JsonProperty("requirement")  val requirement: Requirement
    ) {
        data class Requirement(
            @param:JsonProperty("id") @field:JsonProperty("id")  val id: String
        )

        data class RelatedTenderer(
            @param:JsonProperty("scoring") @field:JsonProperty("scoring")   val id: String
        )

        data class Responder(
            @param:JsonProperty("title") @field:JsonProperty("title") val title: String,
            @param:JsonProperty("name") @field:JsonProperty("name") val name: String,
            @param:JsonProperty("identifier") @field:JsonProperty("identifier")  val identifier: Identifier,
            @param:JsonProperty("businessFunctions") @field:JsonProperty("businessFunctions")  val businessFunctions: List<BusinessFunction>
        ) {
            data class Identifier(
                @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: String,
                @param:JsonProperty("id") @field:JsonProperty("id")  val id: String,
                @JsonInclude(JsonInclude.Include.NON_NULL)
                @param:JsonProperty("uri") @field:JsonProperty("uri") val uri: String?
            )

            data class BusinessFunction(
                @param:JsonProperty("id") @field:JsonProperty("id")  val id: String,
                @param:JsonProperty("type") @field:JsonProperty("type") val type: String,
                @param:JsonProperty("jobTitle") @field:JsonProperty("jobTitle") val jobTitle: String,
                @param:JsonProperty("period") @field:JsonProperty("period") val period: Period,
                @param:JsonProperty("documents") @field:JsonProperty("documents") val documents: List<Document>
            ) {
                data class Period(
                    @param:JsonProperty("startDate") @field:JsonProperty("startDate") val startDate: LocalDateTime
                )

                data class Document(
                    @param:JsonProperty("id") @field:JsonProperty("id")  val id: String,
                    @param:JsonProperty("documentType") @field:JsonProperty("documentType")  val documentType: DocumentType,
                    @param:JsonProperty("title") @field:JsonProperty("title")  val title: String,
                    @JsonInclude(JsonInclude.Include.NON_NULL)
                    @param:JsonProperty("description") @field:JsonProperty("description") val description: String?
                )
            }
        }
    }
}
