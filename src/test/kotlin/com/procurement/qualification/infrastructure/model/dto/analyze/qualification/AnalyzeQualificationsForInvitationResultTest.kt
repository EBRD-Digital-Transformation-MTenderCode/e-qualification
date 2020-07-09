package com.procurement.qualification.infrastructure.model.dto.analyze.qualification

import com.procurement.qualification.infrastructure.handler.analyze.qualification.AnalyzeQualificationsForInvitationResult
import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class AnalyzeQualificationsForInvitationResultTest : AbstractDTOTestBase<AnalyzeQualificationsForInvitationResult>(
    AnalyzeQualificationsForInvitationResult::class.java) {

    @Test
    fun full() {
        testBindingAndMapping(pathToJsonFile = "json/dto/analyze/qualification/analyze_qualifications_for_invitation_result_full.json")
    }

}
