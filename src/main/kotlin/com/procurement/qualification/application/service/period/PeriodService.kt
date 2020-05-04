package com.procurement.qualification.application.service.period

import com.procurement.qualification.application.repository.PeriodRepository
import com.procurement.qualification.application.repository.PeriodRulesRepository
import com.procurement.qualification.application.service.period.strategy.ValidatePeriodStrategy
import com.procurement.qualification.domain.functional.ValidationResult
import com.procurement.qualification.domain.model.data.period.ValidatePeriodContext
import com.procurement.qualification.domain.model.data.period.ValidatePeriodData
import com.procurement.qualification.infrastructure.fail.Fail
import org.springframework.stereotype.Service

@Service
class PeriodService(
    private val periodRepository: PeriodRepository, periodRulesRepository: PeriodRulesRepository
) {
    private val validatePeriodStrategy = ValidatePeriodStrategy(periodRulesRepository)

    fun validatePeriod(data: ValidatePeriodData, context: ValidatePeriodContext): ValidationResult<Fail> =
        validatePeriodStrategy.execute(data = data, context = context)
}