package com.procurement.qualification.infrastructure.model.dto.check.declaration

import com.procurement.qualification.infrastructure.handler.check.declaration.CheckDeclarationRequest
import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class CheckDeclarationRequestTest : AbstractDTOTestBase<CheckDeclarationRequest>(
    CheckDeclarationRequest::class.java) {

    @Test
    fun full() {
        testBindingAndMapping(pathToJsonFile = "json/dto/check/declaration/check_declaration_request_full.json")
    }

}
