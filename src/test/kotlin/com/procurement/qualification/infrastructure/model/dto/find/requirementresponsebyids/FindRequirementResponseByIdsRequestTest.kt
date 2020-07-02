package com.procurement.qualification.infrastructure.model.dto.find.requirementresponsebyids

import com.procurement.qualification.infrastructure.handler.find.requirementresponsebyids.FindRequirementResponseByIdsRequest
import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class FindRequirementResponseByIdsRequestTest : AbstractDTOTestBase<FindRequirementResponseByIdsRequest>(FindRequirementResponseByIdsRequest::class.java) {

    @Test
    fun full() {
        testBindingAndMapping(pathToJsonFile = "json/dto/find/requirementresponsebyids/find_requirement_response_by_ids_request_full.json")
    }

}
