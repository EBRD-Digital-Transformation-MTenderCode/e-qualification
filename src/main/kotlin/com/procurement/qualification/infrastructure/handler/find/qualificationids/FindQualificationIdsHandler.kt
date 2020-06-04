package com.procurement.qualification.infrastructure.handler.find.qualificationids

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.qualification.application.service.Logger
import com.procurement.qualification.application.service.QualificationService
import com.procurement.qualification.application.service.Transform
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.model.qualification.QualificationId
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.handler.general.AbstractHandler2
import com.procurement.qualification.infrastructure.web.enums.Command2Type
import com.procurement.qualification.infrastructure.web.parser.tryGetParams
import org.springframework.stereotype.Component

@Component
class FindQualificationIdsHandler(
    transform: Transform,
    logger: Logger,
    val qualificationService: QualificationService
) : AbstractHandler2<Command2Type, List<QualificationId>>(
    transform = transform,
    logger = logger
) {
    override fun execute(node: JsonNode): Result<List<QualificationId>, Fail> {

        val params = node.tryGetParams(target = FindQualificationIdsRequest::class.java, transform = transform)
            .orForwardFail { fail -> return fail }
            .convert()
            .orForwardFail { fail -> return fail }

        return qualificationService.findQualificationIds(params = params)
    }

    override val action: Command2Type = Command2Type.FIND_QUALIFICATION_IDS
}
