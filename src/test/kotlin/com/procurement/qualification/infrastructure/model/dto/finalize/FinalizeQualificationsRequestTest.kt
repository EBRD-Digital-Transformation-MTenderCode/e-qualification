package com.procurement.qualification.infrastructure.model.dto.finalize

import com.procurement.qualification.infrastructure.handler.finalize.FinalizeQualificationsRequest
import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class FinalizeQualificationsRequestTest : AbstractDTOTestBase<FinalizeQualificationsRequest>(
    FinalizeQualificationsRequest::class.java
) {

    @Test
    fun full() {
        testBindingAndMapping(pathToJsonFile = "json/dto/finalize/finalize_qualifications_request_full.json")
    }
}
