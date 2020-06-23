package com.procurement.qualification.infrastructure.model.dto.create.qualification

import com.procurement.qualification.infrastructure.handler.create.qualifications.CreateQualificationsRequest
import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class CreateQualificationsRequestTest : AbstractDTOTestBase<CreateQualificationsRequest>(CreateQualificationsRequest::class.java) {

    @Test
    fun full() {
        testBindingAndMapping(pathToJsonFile = "json/dto/create.qualification/create_qualifications_request_full.json")
    }

    @Test
    fun full1() {
        testBindingAndMapping(pathToJsonFile = "json/dto/create.qualification/create_qualifications_request_1.json")
    }

    @Test
    fun full2() {
        testBindingAndMapping(pathToJsonFile = "json/dto/create.qualification/create_qualifications_request_2.json")
    }
}
