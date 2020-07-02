package com.procurement.qualification.domain.model.qualification

import com.procurement.qualification.domain.enums.QualificationStatus
import com.procurement.qualification.domain.enums.QualificationStatusDetails
import com.procurement.qualification.domain.model.Owner
import com.procurement.qualification.domain.model.Token
import com.procurement.qualification.domain.model.measure.Scoring
import com.procurement.qualification.domain.model.organization.OrganizationId
import com.procurement.qualification.domain.model.person.PersonId
import com.procurement.qualification.domain.model.requirement.RequirementId
import com.procurement.qualification.domain.model.requirement.RequirementResponseValue
import com.procurement.qualification.domain.model.requirementresponse.RequirementResponseId
import com.procurement.qualification.domain.model.submission.SubmissionId
import java.time.LocalDateTime

data class Qualification(
    val id: QualificationId,
    val date: LocalDateTime,
    val status: QualificationStatus,
    val token: Token,
    val owner: Owner,
    val statusDetails: QualificationStatusDetails? = null,
    val relatedSubmission: SubmissionId,
    val scoring: Scoring?,
    val requirementResponses: List<RequirementResponse> = emptyList()
) {
    data class RequirementResponse(
        val id: RequirementResponseId,
        val value: RequirementResponseValue,
        val relatedTenderer: RelatedTenderer,
        val responder: Responder,
        val requirement: Requirement
    ) {
        data class Requirement(
            val id: RequirementId
        )

        data class RelatedTenderer(
            val id: OrganizationId
        )

        data class Responder(
            val id: PersonId,
            val name: String
        )
    }
}
