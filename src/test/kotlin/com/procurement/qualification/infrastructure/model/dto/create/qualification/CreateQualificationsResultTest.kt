package com.procurement.qualification.infrastructure.model.dto.create.qualification

import com.procurement.qualification.infrastructure.handler.create.qualifications.CreateQualificationsResult
import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class CreateQualificationsResultTest : AbstractDTOTestBase<CreateQualificationsResult>(CreateQualificationsResult::class.java) {

    @Test
    fun full() {
        testBindingAndMapping(pathToJsonFile = "json/dto/create.qualification/create_qualifications_result_full.json")
    }

    @Test
    fun full1() {
        testBindingAndMapping(pathToJsonFile = "json/dto/create.qualification/create_qualifications_result_1.json")
    }

}
