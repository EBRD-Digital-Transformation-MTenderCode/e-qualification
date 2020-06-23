package com.procurement.qualification.infrastructure.model.dto.determine.nextsforqualification

import com.procurement.qualification.infrastructure.handler.determine.nextforqualification.DetermineNextsForQualificationResult
import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class DetermineNextsForQualificationResultTest : AbstractDTOTestBase<DetermineNextsForQualificationResult>(
    DetermineNextsForQualificationResult::class.java) {

    @Test
    fun full() {
        testBindingAndMapping(pathToJsonFile = "json/dto/determine.nextsforqualification/determine_nexts_for_qualification_result_full.json")
    }
}
