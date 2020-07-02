package com.procurement.qualification.infrastructure.model.dto.determine.nextsforqualification

import com.procurement.qualification.infrastructure.handler.determine.nextforqualification.RankQualificationsRequest
import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class RankQualificationsRequestTest : AbstractDTOTestBase<RankQualificationsRequest>(
    RankQualificationsRequest::class.java
) {

    @Test
    fun full() {
        testBindingAndMapping(pathToJsonFile = "json/dto/determine.nextsforqualification/determine_nexts_for_qualification_request_full.json")
    }

    @Test
    fun full1() {
        testBindingAndMapping(pathToJsonFile = "json/dto/determine.nextsforqualification/determine_nexts_for_qualification_request_1.json")
    }

    @Test
    fun full2() {
        testBindingAndMapping(pathToJsonFile = "json/dto/determine.nextsforqualification/determine_nexts_for_qualification_request_2.json")
    }
}
