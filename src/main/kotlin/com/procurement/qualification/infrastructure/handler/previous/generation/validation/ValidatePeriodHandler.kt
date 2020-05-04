package com.procurement.qualification.infrastructure.handler.previous.generation.validation

import com.procurement.qualification.application.service.Logger
import com.procurement.qualification.application.service.period.PeriodService
import com.procurement.qualification.domain.functional.ValidationResult
import com.procurement.qualification.infrastructure.converter.convert
import com.procurement.qualification.infrastructure.converter.toValidatePeriodContext
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.web.dto.command.CommandMessage
import com.procurement.qualification.infrastructure.web.dto.command.CommandType
import com.procurement.qualification.infrastructure.web.dto.request.period.ValidatePeriodRequest
import com.procurement.qualification.infrastructure.web.parser.tryGetData
import org.springframework.stereotype.Component

@Component
class ValidatePeriodHandler(
    private val periodService: PeriodService, logger: Logger
) : AbstractValidationHandler<CommandType, Fail>(logger) {

    override val action: CommandType = CommandType.VALIDATE_PERIOD

    override fun execute(cm: CommandMessage): ValidationResult<Fail> {
        val data = cm.data.tryGetData(ValidatePeriodRequest::class.java)
            .doReturn { error -> return ValidationResult.error(error) }
            .convert()
            .doReturn { errors -> return ValidationResult.error(errors) }

        val context = cm.toValidatePeriodContext()
            .doReturn { error -> return ValidationResult.error(error) }

        return periodService.validatePeriod(data, context)
    }
}