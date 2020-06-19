package com.procurement.qualification.infrastructure.model.dto.check.accesstoqualification

import com.procurement.qualification.infrastructure.handler.check.accesstoqualification.CheckAccessToQualificationRequest
import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class CheckAccessToQualificationRequestTest : AbstractDTOTestBase<CheckAccessToQualificationRequest>(
    CheckAccessToQualificationRequest::class.java) {

    @Test
    fun full() {
        testBindingAndMapping(pathToJsonFile = "json/dto/check.accesstoqualification/check_access_to_qualification_request_full.json")
    }

}
