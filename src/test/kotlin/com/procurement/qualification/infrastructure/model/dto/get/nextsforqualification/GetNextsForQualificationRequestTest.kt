package com.procurement.qualification.infrastructure.model.dto.get.nextsforqualification

import com.procurement.qualification.infrastructure.handler.get.nextforqualification.GetNextsForQualificationRequest
import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class GetNextsForQualificationRequestTest : AbstractDTOTestBase<GetNextsForQualificationRequest>(GetNextsForQualificationRequest::class.java) {

    @Test
    fun full() {
        testBindingAndMapping(pathToJsonFile = "json/dto/get.nextsforqualification/get_nexts_for_qualification_request_full.json")
    }

    @Test
    fun full1() {
        testBindingAndMapping(pathToJsonFile = "json/dto/get.nextsforqualification/get_nexts_for_qualification_request_1.json")
    }
}
