package com.procurement.qualification.infrastructure.model.dto.period.check

import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import com.procurement.qualification.infrastructure.web.dto.request.period.CheckPeriod2Request
import org.junit.jupiter.api.Test

class CheckPeriod2RequestTest : AbstractDTOTestBase<CheckPeriod2Request>(CheckPeriod2Request::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/dto/period/check/request_check_period2.json")
    }
}
