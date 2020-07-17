package com.procurement.qualification.infrastructure.model.dto.check.qualification.protocol

import com.procurement.qualification.infrastructure.handler.check.qualification.protocol.CheckQualificationsForProtocolRequest
import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class CheckQualificationsForProtocolRequestTest : AbstractDTOTestBase<CheckQualificationsForProtocolRequest>(
    CheckQualificationsForProtocolRequest::class.java) {

    @Test
    fun full() {
        testBindingAndMapping(pathToJsonFile = "json/dto/check/qualification/protocol/check_qualifications_for_protocol_request_full.json")
    }

}
