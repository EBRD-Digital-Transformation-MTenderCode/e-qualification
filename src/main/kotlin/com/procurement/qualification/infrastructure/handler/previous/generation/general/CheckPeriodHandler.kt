package com.procurement.qualification.infrastructure.handler.previous.generation.general

import com.procurement.qualification.application.model.period.check.CheckPeriodResult
import com.procurement.qualification.application.service.Logger
import com.procurement.qualification.application.service.period.PeriodService
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.infrastructure.converter.convert
import com.procurement.qualification.infrastructure.converter.toCheckPeriodContext
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.web.dto.command.CommandMessage
import com.procurement.qualification.infrastructure.web.dto.command.CommandType
import com.procurement.qualification.infrastructure.web.dto.request.period.CheckPeriodRequest
import com.procurement.qualification.infrastructure.web.parser.tryGetData
import org.springframework.stereotype.Component

@Component
class CheckPeriodHandler(
    private val periodService: PeriodService, logger: Logger
) : AbstractHandler<CommandType, CheckPeriodResult>(logger) {

    override val action: CommandType = CommandType.CHECK_PERIOD

    override fun execute(cm: CommandMessage): Result<CheckPeriodResult, Fail> {
        val data = cm.data.tryGetData(CheckPeriodRequest::class.java)
            .orForwardFail { error -> return error }
            .convert()
            .orForwardFail { error -> return error }

        val context = cm.toCheckPeriodContext()
            .orForwardFail { error -> return error }

        return periodService.checkPeriod(data, context)
    }
}