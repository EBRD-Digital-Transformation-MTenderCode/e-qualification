package com.procurement.qualification.infrastructure.handler.analyze.qualification


import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.qualification.domain.enums.QualificationStatus
import com.procurement.qualification.domain.enums.QualificationStatusDetails
import com.procurement.qualification.domain.model.qualification.QualificationId
import com.procurement.qualification.domain.model.submission.SubmissionId

data class AnalyzeQualificationsForInvitationResult(
    @param:JsonProperty("qualifications") @field:JsonProperty("qualifications") val qualifications: List<Qualification>
) {
    data class Qualification(
        @param:JsonProperty("id") @field:JsonProperty("id") val id: QualificationId,
        @param:JsonProperty("status") @field:JsonProperty("status") val status: QualificationStatus,
        @param:JsonProperty("statusDetails") @field:JsonProperty("statusDetails") val statusDetails: QualificationStatusDetails,
        @param:JsonProperty("relatedSubmission") @field:JsonProperty("relatedSubmission") val relatedSubmission: SubmissionId
    )
}