package com.procurement.qualification.infrastructure.model.dto.create.qualification

import com.procurement.qualification.infrastructure.handler.create.qualification.DoQualificationRequest
import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class DoQualificationRequestTest : AbstractDTOTestBase<DoQualificationRequest>(DoQualificationRequest::class.java) {

    @Test
    fun full() {
        testBindingAndMapping(pathToJsonFile = "json/dto/create/qualification/do_qualification_request_full.json")
    }

    @Test
    fun full1() {
        testBindingAndMapping(pathToJsonFile = "json/dto/create/qualification/do_qualification_request_1.json")
    }

    @Test
    fun full2() {
        testBindingAndMapping(pathToJsonFile = "json/dto/create/qualification/do_qualification_request_2.json")
    }
}
