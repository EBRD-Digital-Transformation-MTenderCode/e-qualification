package com.procurement.qualification.infrastructure.model.dto.check.qualification.period

import com.procurement.qualification.infrastructure.handler.check.qualification.period.CheckQualificationPeriodRequest
import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class CheckQualificationPeriodRequestTest : AbstractDTOTestBase<CheckQualificationPeriodRequest>(
    CheckQualificationPeriodRequest::class.java) {

    @Test
    fun full() {
        testBindingAndMapping(pathToJsonFile = "json/dto/check/qualification/period/check_qualification_period_request_full.json")
    }

}
