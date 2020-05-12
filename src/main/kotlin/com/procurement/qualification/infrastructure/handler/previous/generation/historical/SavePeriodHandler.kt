package com.procurement.qualification.infrastructure.handler.previous.generation.historical

import com.procurement.qualification.application.model.period.save.SavePeriodResult
import com.procurement.qualification.application.repository.HistoryRepository
import com.procurement.qualification.application.service.Logger
import com.procurement.qualification.application.service.Transform
import com.procurement.qualification.application.service.period.PeriodService
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.infrastructure.converter.convert
import com.procurement.qualification.infrastructure.converter.toSavePeriodContext
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.web.dto.command.CommandMessage
import com.procurement.qualification.infrastructure.web.dto.command.CommandType
import com.procurement.qualification.infrastructure.web.dto.request.period.SavePeriodRequest
import com.procurement.qualification.infrastructure.web.parser.tryGetData
import org.springframework.stereotype.Component

@Component
class SavePeriodHandler(
    private val periodService: PeriodService, logger: Logger, historyRepository: HistoryRepository, transform: Transform
) : AbstractHistoricalHandler<CommandType, SavePeriodResult>(
    logger = logger, historyRepository = historyRepository, target = SavePeriodResult::class.java, transform = transform
) {

    override val action: CommandType = CommandType.SAVE_PERIOD

    override fun execute(cm: CommandMessage): Result<SavePeriodResult, Fail> {
        val data = cm.data.tryGetData(SavePeriodRequest::class.java)
            .orForwardFail { error -> return error }
            .convert()
            .orForwardFail { errors -> return errors }

        val context = cm.toSavePeriodContext()
            .orForwardFail { error -> return error }

        return periodService.savePeriod(data, context)
    }
}
