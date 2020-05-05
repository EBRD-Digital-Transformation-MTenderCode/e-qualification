package com.procurement.qualification.application.service.period.strategy

import com.procurement.qualification.application.model.period.check.CheckPeriodContext
import com.procurement.qualification.application.model.period.check.CheckPeriodData
import com.procurement.qualification.application.model.period.check.CheckPeriodResult
import com.procurement.qualification.application.repository.PeriodRepository
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.Result.Companion.failure
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.fail.error.ValidationError
import com.procurement.qualification.infrastructure.model.entity.PeriodEntity

class CheckPeriodStrategy(private val periodRepository: PeriodRepository) {

    fun execute(data: CheckPeriodData, context: CheckPeriodContext): Result<CheckPeriodResult, Fail> {
        val requestPeriod = data.period
        val storedPeriod = periodRepository.findBy(cpid = context.cpid, ocid = context.ocid)
            .orForwardFail { incident -> return incident }!!

        requestPeriod
            .checkDates()
            .doReturn { error -> return failure(error) }
            .compareWith(storedPeriod = storedPeriod)
            .doOnError { error -> return failure(error) }

        return generateCheckPeriodResult(requestPeriod = requestPeriod, storedPeriod = storedPeriod).asSuccess()
    }

    private fun CheckPeriodData.Period.checkDates(): Result<CheckPeriodData.Period, Fail> =
        if (!startDate.isBefore(endDate))
            failure(ValidationError.CommandError.InvalidPeriodOnCheckPeriod())
        else asSuccess()

    private fun CheckPeriodData.Period.compareWith(storedPeriod: PeriodEntity): Result<CheckPeriodData.Period, Fail> =
        if (endDate.isBefore(storedPeriod.endDate))
            failure(ValidationError.CommandError.InvalidPeriodEndDate())
        else asSuccess()

    private fun generateCheckPeriodResult(
        requestPeriod: CheckPeriodData.Period,
        storedPeriod: PeriodEntity
    ): CheckPeriodResult {
        val isPreQualificationPeriodChanged = !requestPeriod.endDate.isEqual(storedPeriod.endDate)

        return CheckPeriodResult(
            preQualificationPeriodChanged = isPreQualificationPeriodChanged,
            preQualification = CheckPeriodResult.PreQualification(
                period = CheckPeriodResult.PreQualification.Period(
                    startDate = storedPeriod.startDate,
                    endDate = requestPeriod.endDate
                )
            )
        )
    }
}
