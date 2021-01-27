package com.procurement.qualification.infrastructure.model.dto.determine.nextsforqualification

import com.procurement.qualification.infrastructure.handler.determine.nextforqualification.RankQualificationsRequest
import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class RankQualificationsRequestTest : AbstractDTOTestBase<RankQualificationsRequest>(
    RankQualificationsRequest::class.java
) {

    @Test
    fun full() {
        testBindingAndMapping(pathToJsonFile = "json/dto/rank.qualification/rank_qualifications_request_full.json")
    }

    @Test
    fun full1() {
        testBindingAndMapping(pathToJsonFile = "json/dto/rank.qualification/rank_qualifications_request_1.json")
    }
}
