package com.procurement.qualification.infrastructure.handler.find.requirementresponsebyids

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
class FindRequirementResponseByIdsHandler(
    transform: Transform,
    logger: Logger,
    val qualificationService: QualificationService
) : AbstractHandler2<Command2Type, FindRequirementResponseByIdsResult>(
    transform = transform,
    logger = logger
) {
    override fun execute(node: JsonNode): Result<FindRequirementResponseByIdsResult, Fail> {

        val params = node.tryGetParams(target = FindRequirementResponseByIdsRequest::class.java, transform = transform)
            .orForwardFail { fail -> return fail }
            .convert()
            .orForwardFail { fail -> return fail }

        return qualificationService.findRequirementResponseByIds(params = params)
    }

    override val action: Command2Type = Command2Type.FIND_REQUIREMENT_RESPONSE_BY_IDS
}
