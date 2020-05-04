package com.procurement.qualification.infrastructure.model.dto.period.validate

import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import com.procurement.qualification.infrastructure.web.dto.request.period.ValidatePeriodRequest
import org.junit.jupiter.api.Test

class ValidatePeriodRequestTest : AbstractDTOTestBase<ValidatePeriodRequest>(ValidatePeriodRequest::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/dto/period/validate/request_validate_period.json")
    }
}
