package com.procurement.qualification.infrastructure.model.dto.api.create.qualification

import com.procurement.qualification.infrastructure.handler.create.qualification.CreateQualificationResult
import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class CreateQualificationResultTest : AbstractDTOTestBase<CreateQualificationResult>(CreateQualificationResult::class.java) {

    @Test
    fun full() {
        testBindingAndMapping(pathToJsonFile = "json/dto/create.qualification/create_qualification_result_full.json")
    }

    @Test
    fun full1() {
        testBindingAndMapping(pathToJsonFile = "json/dto/create.qualification/create_qualification_result_1.json")
    }

}
