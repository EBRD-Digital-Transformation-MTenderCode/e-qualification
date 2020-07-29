package com.procurement.qualification.infrastructure.model.dto.set

import com.procurement.qualification.infrastructure.handler.set.SetQualificationPeriodEndResult
import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class SetQualificationPeriodEndResultTest : AbstractDTOTestBase<SetQualificationPeriodEndResult>(
    SetQualificationPeriodEndResult::class.java
) {

    @Test
    fun full() {
        testBindingAndMapping(pathToJsonFile = "json/dto/set/set_qualification_period_end_result_full.json")
    }
}
