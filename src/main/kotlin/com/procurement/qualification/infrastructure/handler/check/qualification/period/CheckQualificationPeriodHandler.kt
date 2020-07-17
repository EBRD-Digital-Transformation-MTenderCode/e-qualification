package com.procurement.qualification.infrastructure.handler.check.qualification.period

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.qualification.application.service.Logger
import com.procurement.qualification.application.service.PeriodService
import com.procurement.qualification.application.service.Transform
import com.procurement.qualification.domain.functional.ValidationResult
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.handler.validation.AbstractValidationHandler2
import com.procurement.qualification.infrastructure.web.enums.Command2Type
import com.procurement.qualification.infrastructure.web.parser.tryGetParams
import org.springframework.stereotype.Component

@Component
class CheckQualificationPeriodHandler(
    val transform: Transform,
    logger: Logger,
    val periodService: PeriodService
) : AbstractValidationHandler2<Command2Type, Fail>(logger = logger) {

    override fun execute(node: JsonNode): ValidationResult<Fail> {

        val params = node.tryGetParams(target = CheckQualificationPeriodRequest::class.java, transform = transform)
            .doReturn { fail -> return ValidationResult.error(fail) }
            .convert()
            .doReturn { fail -> return ValidationResult.error(fail) }

        return periodService.checkQualificationPeriod(params = params)
    }

    override val action: Command2Type = Command2Type.CHECK_QUALIFICATION_PERIOD
}
