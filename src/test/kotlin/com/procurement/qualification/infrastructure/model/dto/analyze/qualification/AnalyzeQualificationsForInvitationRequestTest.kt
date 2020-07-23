package com.procurement.qualification.infrastructure.model.dto.analyze.qualification

import com.procurement.qualification.infrastructure.handler.analyze.qualification.AnalyzeQualificationsForInvitationRequest
import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class AnalyzeQualificationsForInvitationRequestTest : AbstractDTOTestBase<AnalyzeQualificationsForInvitationRequest>(
    AnalyzeQualificationsForInvitationRequest::class.java) {

    @Test
    fun full() {
        testBindingAndMapping(pathToJsonFile = "json/dto/analyze/qualification/analyze_qualifications_for_invitation_request_full.json")
    }

}
