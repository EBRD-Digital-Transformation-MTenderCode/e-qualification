package com.procurement.qualification.domain.model.qualification

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
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
    @param:JsonProperty("scoring") @field:JsonProperty("scoring") val scoring: Scoring?
)
