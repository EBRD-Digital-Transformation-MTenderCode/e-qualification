package com.procurement.qualification.infrastructure.model.dto.set.nextforqualification

import com.procurement.qualification.infrastructure.handler.set.nextforqualification.SetNextForQualificationResult
import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class SetNextForQualificationResultTest : AbstractDTOTestBase<SetNextForQualificationResult>(
    SetNextForQualificationResult::class.java
) {

    @Test
    fun full() {
        testBindingAndMapping(pathToJsonFile = "json/dto/set/nextforqualification/set_next_for_qualification_result_full.json")
    }

    @Test
    fun full1() {
        testBindingAndMapping(pathToJsonFile = "json/dto/set/nextforqualification/set_next_for_qualification_result_1.json")
    }

    @Test
    fun full2() {
        testBindingAndMapping(pathToJsonFile = "json/dto/set/nextforqualification/set_next_for_qualification_result_2.json")
    }
    @Test
    fun full3() {
        testBindingAndMapping(pathToJsonFile = "json/dto/set/nextforqualification/set_next_for_qualification_result_3.json")
    }
}
