package com.procurement.qualification.infrastructure.handler.create.consideration

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.qualification.application.repository.HistoryRepository
import com.procurement.qualification.application.service.Logger
import com.procurement.qualification.application.service.QualificationService
import com.procurement.qualification.application.service.Transform
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.handler.historical.AbstractHistoricalHandler2
import com.procurement.qualification.infrastructure.web.enums.Command2Type
import com.procurement.qualification.infrastructure.web.parser.tryGetParams
import org.springframework.stereotype.Component

@Component
class DoConsiderationHandler(
    historyRepository: HistoryRepository,
    transform: Transform,
    logger: Logger,
    private val qualificationService: QualificationService
) : AbstractHistoricalHandler2<Command2Type, DoConsiderationResult>(
    target = DoConsiderationResult::class.java,
    historyRepository = historyRepository,
    transform = transform,
    logger = logger
) {
    override fun execute(node: JsonNode): Result<DoConsiderationResult, Fail> {

        val params = node.tryGetParams(target = DoConsiderationRequest::class.java, transform = transform)
            .orForwardFail { fail -> return fail }
            .convert()
            .orForwardFail { fail -> return fail }

        return qualificationService.doConsideration(params = params)
    }

    override val action: Command2Type = Command2Type.DO_CONSIDERATION
}
