package com.procurement.qualification.infrastructure.model.dto.find.qualificationIds

import com.procurement.qualification.infrastructure.handler.find.qualificationids.FindQualificationIdsRequest
import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class FindQualificationByIdsRequestTest : AbstractDTOTestBase<FindQualificationIdsRequest>(FindQualificationIdsRequest::class.java) {

    @Test
    fun full() {
        testBindingAndMapping(pathToJsonFile = "json/dto/find/qualificationids/find_qualification_by_ids_request_full.json")
    }

    @Test
    fun full1() {
        testBindingAndMapping(pathToJsonFile = "json/dto/find/qualificationids/find_qualification_by_ids_request_1.json")
    }
}
