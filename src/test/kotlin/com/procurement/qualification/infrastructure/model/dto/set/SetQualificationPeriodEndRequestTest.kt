package com.procurement.qualification.infrastructure.model.dto.set

import com.procurement.qualification.infrastructure.handler.set.SetQualificationPeriodEndRequest
import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class SetQualificationPeriodEndRequestTest : AbstractDTOTestBase<SetQualificationPeriodEndRequest>(
    SetQualificationPeriodEndRequest::class.java
) {

    @Test
    fun full() {
        testBindingAndMapping(pathToJsonFile = "json/dto/set/set_qualification_period_end_request_full.json")
    }
}
