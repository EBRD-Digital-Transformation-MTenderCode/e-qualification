package com.procurement.qualification.infrastructure.handler.start.qualificationperiod

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.qualification.application.repository.HistoryRepository
import com.procurement.qualification.application.service.Logger
import com.procurement.qualification.application.service.PeriodService
import com.procurement.qualification.application.service.Transform
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.handler.historical.AbstractHistoricalHandler2
import com.procurement.qualification.infrastructure.web.enums.Command2Type
import com.procurement.qualification.infrastructure.web.parser.tryGetParams
import org.springframework.stereotype.Component

@Component
class StartQualificationPeriodHandler(
    historyRepository: HistoryRepository,
    transform: Transform,
    logger: Logger,
    private val periodService: PeriodService
) : AbstractHistoricalHandler2<Command2Type, StartQualificationPeriodResult>(
    target = StartQualificationPeriodResult::class.java,
    historyRepository = historyRepository,
    transform = transform,
    logger = logger
) {
    override fun execute(node: JsonNode): Result<StartQualificationPeriodResult, Fail> {

        val params = node.tryGetParams(target = StartQualificationPeriodRequest::class.java, transform = transform)
            .orForwardFail { fail -> return fail }
            .convert()
            .orForwardFail { fail -> return fail }

        return periodService.startQualificationPeriod(params = params)
    }

    override val action: Command2Type = Command2Type.START_QUALIFICATION_PERIOD
}
