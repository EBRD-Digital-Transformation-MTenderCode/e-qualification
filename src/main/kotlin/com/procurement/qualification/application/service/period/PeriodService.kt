package com.procurement.qualification.application.service.period

import com.procurement.qualification.application.model.period.check.CheckPeriodContext
import com.procurement.qualification.application.model.period.check.CheckPeriodData
import com.procurement.qualification.application.model.period.check.CheckPeriodResult
import com.procurement.qualification.application.model.period.save.SavePeriodContext
import com.procurement.qualification.application.model.period.save.SavePeriodData
import com.procurement.qualification.application.model.period.save.SavePeriodResult
import com.procurement.qualification.application.model.period.validate.ValidatePeriodContext
import com.procurement.qualification.application.model.period.validate.ValidatePeriodData
import com.procurement.qualification.application.repository.PeriodRepository
import com.procurement.qualification.application.service.period.strategy.CheckPeriodStrategy
import com.procurement.qualification.application.service.period.strategy.ValidatePeriodStrategy
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.Result.Companion.failure
import com.procurement.qualification.domain.functional.ValidationResult
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.model.entity.PeriodEntity
import org.springframework.stereotype.Service

@Service
class PeriodService(
    private val periodRepository: PeriodRepository,
    private val validatePeriodStrategy: ValidatePeriodStrategy,
    private val checkPeriodStrategy: CheckPeriodStrategy
) {
    fun validatePeriod(data: ValidatePeriodData, context: ValidatePeriodContext): ValidationResult<Fail> =
        validatePeriodStrategy.execute(data = data, context = context)

    fun savePeriod(data: SavePeriodData, context: SavePeriodContext): Result<SavePeriodResult, Fail> {
        periodRepository.saveOrUpdatePeriod(
            PeriodEntity(
                cpid = context.cpid,
                ocid = context.ocid,
                endDate = data.period.endDate,
                startDate = data.period.startDate
            )
        ).doOnError { incident -> return failure(incident) }

        return SavePeriodResult.asSuccess()
    }

    fun checkPeriod(data: CheckPeriodData, context: CheckPeriodContext): Result<CheckPeriodResult, Fail> =
        checkPeriodStrategy.execute(data = data, context = context)
}