package com.procurement.qualification.infrastructure.handler.validation

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.qualification.application.service.Logger
import com.procurement.qualification.application.service.period.PeriodService
import com.procurement.qualification.domain.functional.ValidationResult
import com.procurement.qualification.infrastructure.converter.convert
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.web.dto.request.period.CheckPeriod2Request
import com.procurement.qualification.infrastructure.web.enums.Command2Type
import com.procurement.qualification.infrastructure.web.parser.tryGetParams
import org.springframework.stereotype.Component

@Component
class CheckPeriod2Handler(
    private val periodService: PeriodService, logger: Logger
) : AbstractValidationHandler2<Command2Type, Fail>(logger) {

    override val action: Command2Type = Command2Type.CHECK_PERIOD_2

    override fun execute(node: JsonNode): ValidationResult<Fail> {
        val data = node.tryGetParams(CheckPeriod2Request::class.java)
            .doReturn { error -> return ValidationResult.error(error) }
            .convert()
            .doReturn { error -> return ValidationResult.error(error) }

        return periodService.checkPeriodDate(data)
    }
}