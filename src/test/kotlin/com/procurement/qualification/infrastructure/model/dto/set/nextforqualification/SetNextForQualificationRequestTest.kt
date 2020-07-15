package com.procurement.qualification.infrastructure.model.dto.set.nextforqualification

import com.procurement.qualification.infrastructure.handler.set.nextforqualification.SetNextForQualificationRequest
import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class SetNextForQualificationRequestTest : AbstractDTOTestBase<SetNextForQualificationRequest>(
    SetNextForQualificationRequest::class.java
) {

    @Test
    fun full() {
        testBindingAndMapping(pathToJsonFile = "json/dto/set/nextforqualification/set_next_for_qualification_request_full.json")
    }

    @Test
    fun full1() {
        testBindingAndMapping(pathToJsonFile = "json/dto/set/nextforqualification/set_next_for_qualification_request_1.json")
    }

    @Test
    fun full2() {
        testBindingAndMapping(pathToJsonFile = "json/dto/set/nextforqualification/set_next_for_qualification_request_2.json")
    }
}
