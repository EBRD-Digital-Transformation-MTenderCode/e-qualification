package com.procurement.qualification.infrastructure.model.dto.check.qualification.state

import com.procurement.qualification.infrastructure.handler.check.qualification.state.CheckQualificationStateRequest
import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class CheckQualificationStateTest : AbstractDTOTestBase<CheckQualificationStateRequest>(
    CheckQualificationStateRequest::class.java) {

    @Test
    fun full() {
        testBindingAndMapping(pathToJsonFile = "json/dto/check/qualification/state/check_qualification_state_request_full.json")
    }

}
