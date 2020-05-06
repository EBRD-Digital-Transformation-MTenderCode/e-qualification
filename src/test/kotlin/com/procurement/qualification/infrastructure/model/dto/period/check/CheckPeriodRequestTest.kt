package com.procurement.qualification.infrastructure.model.dto.period.check

import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import com.procurement.qualification.infrastructure.web.dto.request.period.CheckPeriodRequest
import org.junit.jupiter.api.Test

class CheckPeriodRequestTest : AbstractDTOTestBase<CheckPeriodRequest>(CheckPeriodRequest::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/dto/period/check/request_check_period.json")
    }
}
