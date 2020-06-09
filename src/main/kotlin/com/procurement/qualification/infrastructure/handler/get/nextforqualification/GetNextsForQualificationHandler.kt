package com.procurement.qualification.infrastructure.handler.get.nextforqualification

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
class GetNextsForQualificationHandler(
    transform: Transform,
    logger: Logger,
    val qualificationService: QualificationService
) : AbstractHandler2<Command2Type, List<GetNextsForQualificationResult>>(
    transform = transform,
    logger = logger
) {
    override fun execute(node: JsonNode): Result<List<GetNextsForQualificationResult>, Fail> {

        val params = node.tryGetParams(target = GetNextsForQualificationRequest::class.java, transform = transform)
            .orForwardFail { fail -> return fail }
            .convert()
            .orForwardFail { fail -> return fail }

        return qualificationService.getNextsForQualification(params = params)
    }

    override val action: Command2Type = Command2Type.GET_NEXTS_FOR_QUALIFICATION
}
