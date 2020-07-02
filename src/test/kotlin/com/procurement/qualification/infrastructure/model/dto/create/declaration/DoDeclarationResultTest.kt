package com.procurement.qualification.infrastructure.model.dto.create.declaration

import com.procurement.qualification.infrastructure.handler.create.declaration.DoDeclarationResult
import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class DoDeclarationResultTest : AbstractDTOTestBase<DoDeclarationResult>(DoDeclarationResult::class.java) {

    @Test
    fun full() {
        testBindingAndMapping(pathToJsonFile = "json/dto/create.declaration/do_declaration_result_full.json")
    }

}
