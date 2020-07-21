package com.procurement.qualification.infrastructure.model.dto.finalize

import com.procurement.qualification.infrastructure.handler.finalize.FinalizeQualificationsResult
import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class FinalizeQualificationsResultTest : AbstractDTOTestBase<FinalizeQualificationsResult>(
    FinalizeQualificationsResult::class.java
) {

    @Test
    fun full() {
        testBindingAndMapping(pathToJsonFile = "json/dto/finalize/finalize_qualifications_result_full.json")
    }
}
