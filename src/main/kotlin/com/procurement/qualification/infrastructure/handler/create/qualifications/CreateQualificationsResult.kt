package com.procurement.qualification.infrastructure.handler.create.qualifications


import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.qualification.domain.enums.QualificationStatus
import com.procurement.qualification.domain.model.Token
import com.procurement.qualification.domain.model.measure.Scoring
import com.procurement.qualification.domain.model.qualification.QualificationId
import com.procurement.qualification.domain.model.submission.SubmissionId
import java.time.LocalDateTime

data class CreateQualificationsResult(
    @field:JsonProperty("id") @param:JsonProperty("id") val id: QualificationId,
    @field:JsonProperty("status") @param:JsonProperty("status") val status: QualificationStatus,
    @field:JsonProperty("relatedSubmission") @param:JsonProperty("relatedSubmission") val relatedSubmission: SubmissionId,
    @field:JsonProperty("date") @param:JsonProperty("date") val date: LocalDateTime,
    @field:JsonProperty("token") @param:JsonProperty("token") val token: Token,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @field:JsonProperty("scoring") @param:JsonProperty("scoring") val scoring: Scoring?
)
