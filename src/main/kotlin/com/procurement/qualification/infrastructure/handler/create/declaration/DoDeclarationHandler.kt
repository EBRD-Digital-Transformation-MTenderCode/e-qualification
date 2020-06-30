package com.procurement.qualification.infrastructure.handler.create.declaration

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.qualification.application.service.Logger
import com.procurement.qualification.application.service.QualificationService
import com.procurement.qualification.application.service.Transform
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.handler.general.AbstractHandler2
import com.procurement.qualification.infrastructure.web.enums.Command2Type
import com.procurement.qualification.infrastructure.web.parser.tryGetParams
import org.springframework.stereotype.Component

@Component
class DoDeclarationHandler(
    transform: Transform,
    logger: Logger,
    val qualificationService: QualificationService
) : AbstractHandler2<Command2Type, DoDeclarationResult>(
    transform = transform,
    logger = logger
) {
    override fun execute(node: JsonNode): Result<DoDeclarationResult, Fail> {

        val params = node.tryGetParams(target = DoDeclarationRequest::class.java, transform = transform)
            .orForwardFail { fail -> return fail }
            .convert()
            .orForwardFail { fail -> return fail }

        return qualificationService.doDeclaration(params = params)
    }

    override val action: Command2Type = Command2Type.DO_DECLARATION
}
