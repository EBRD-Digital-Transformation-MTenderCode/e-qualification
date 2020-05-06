package com.procurement.qualification.infrastructure.model.dto.period.save

import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import com.procurement.qualification.infrastructure.web.dto.request.period.SavePeriodRequest
import org.junit.jupiter.api.Test

class SavePeriodRequestTest : AbstractDTOTestBase<SavePeriodRequest>(SavePeriodRequest::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/dto/period/save/request_save_period.json")
    }
}
