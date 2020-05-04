package com.procurement.qualification.application.service.period.strategy

import com.procurement.qualification.application.model.period.validate.ValidatePeriodContext
import com.procurement.qualification.application.model.period.validate.ValidatePeriodData
import com.procurement.qualification.application.repository.PeriodRulesRepository
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.Result.Companion.failure
import com.procurement.qualification.domain.functional.ValidationResult
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.fail.error.ValidationError
import java.time.temporal.ChronoUnit

class ValidatePeriodStrategy(private val periodRulesRepository: PeriodRulesRepository) {

    fun execute(data: ValidatePeriodData, context: ValidatePeriodContext): ValidationResult<Fail> {
        data.period
            .checkDates()
            .doReturn { fail -> return ValidationResult.error(fail) }
            .checkTerm(context)
            .doOnError { fail -> return ValidationResult.error(fail) }

        return ValidationResult.ok()
    }

    private fun ValidatePeriodData.Period.checkTerm(context: ValidatePeriodContext): Result<ValidatePeriodData.Period, Fail> {
        val expectedDuration = periodRulesRepository.findTermBy(country = context.country, pmd = context.pmd)
            .orForwardFail { incident -> return incident }
            ?: return failure(ValidationError.CommandError.PeriodRuleNotFound(country = context.country, pmd = context.pmd))

        val actualDuration = ChronoUnit.SECONDS.between(startDate, endDate)

        if (actualDuration < expectedDuration)
            return failure(ValidationError.CommandError.InvalidPeriodTerm())

        return this.asSuccess()
    }

    private fun ValidatePeriodData.Period.checkDates(): Result<ValidatePeriodData.Period, Fail> {
        if (!startDate.isBefore(endDate))
            return failure(ValidationError.CommandError.InvalidPeriod())

        return this.asSuccess()
    }
}
