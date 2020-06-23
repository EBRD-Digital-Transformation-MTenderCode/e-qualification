package com.procurement.qualification.infrastructure.model.dto.find.requirementresponsebyids

import com.procurement.qualification.infrastructure.handler.find.requirementresponsebyids.FindRequirementResponseByIdsResult
import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class FindRequirementResponseByIdsResultTest : AbstractDTOTestBase<FindRequirementResponseByIdsResult>(FindRequirementResponseByIdsResult::class.java) {

    @Test
    fun full() {
        testBindingAndMapping(pathToJsonFile = "json/dto/find/requirementresponsebyids/find_requirement_response_by_ids_result_full.json")
    }

}
