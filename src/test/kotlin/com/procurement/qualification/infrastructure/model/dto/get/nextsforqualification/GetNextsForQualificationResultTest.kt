package com.procurement.qualification.infrastructure.model.dto.get.nextsforqualification

import com.procurement.qualification.infrastructure.handler.get.nextforqualification.GetNextsForQualificationResult
import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class GetNextsForQualificationResultTest : AbstractDTOTestBase<GetNextsForQualificationResult>(GetNextsForQualificationResult::class.java) {

    @Test
    fun full() {
        testBindingAndMapping(pathToJsonFile = "json/dto/get.nextsforqualification/get_nexts_for_qualification_result_full.json")
    }
}
