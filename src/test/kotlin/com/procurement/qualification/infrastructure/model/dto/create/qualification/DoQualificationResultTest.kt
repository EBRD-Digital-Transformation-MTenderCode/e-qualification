package com.procurement.qualification.infrastructure.model.dto.create.qualification

import com.procurement.qualification.infrastructure.handler.create.qualification.DoQualificationResult
import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class DoQualificationResultTest : AbstractDTOTestBase<DoQualificationResult>(DoQualificationResult::class.java) {

    @Test
    fun full() {
        testBindingAndMapping(pathToJsonFile = "json/dto/create/qualification/do_qualification_result_full.json")
    }

    @Test
    fun full1() {
        testBindingAndMapping(pathToJsonFile = "json/dto/create/qualification/do_qualification_result_1.json")
    }

    @Test
    fun full2() {
        testBindingAndMapping(pathToJsonFile = "json/dto/create/qualification/do_qualification_result_2.json")
    }
}
