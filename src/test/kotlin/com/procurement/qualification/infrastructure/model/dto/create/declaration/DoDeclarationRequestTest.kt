package com.procurement.qualification.infrastructure.model.dto.create.declaration

import com.procurement.qualification.infrastructure.handler.create.declaration.DoDeclarationRequest
import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class DoDeclarationRequestTest : AbstractDTOTestBase<DoDeclarationRequest>(DoDeclarationRequest::class.java) {

    @Test
    fun full() {
        testBindingAndMapping(pathToJsonFile = "json/dto/create.declaration/do_declaration_request_full.json")
    }

}
