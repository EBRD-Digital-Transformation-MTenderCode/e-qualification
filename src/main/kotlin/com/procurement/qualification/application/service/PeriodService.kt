package com.procurement.qualification.application.service

import com.procurement.qualification.application.model.params.StartQualificationPeriodParams
import com.procurement.qualification.application.repository.PeriodRepository
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.ValidationResult
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.functional.asValidationFailure
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.fail.error.ValidationError
import com.procurement.qualification.infrastructure.handler.check.qualification.period.CheckQualificationPeriodParams
import com.procurement.qualification.infrastructure.handler.start.qualificationperiod.StartQualificationPeriodResult
import com.procurement.qualification.infrastructure.model.entity.PeriodEntity
import org.springframework.stereotype.Service

interface PeriodService {

    fun startQualificationPeriod(params: StartQualificationPeriodParams): Result<StartQualificationPeriodResult, Fail>
    fun checkQualificationPeriod(params: CheckQualificationPeriodParams): ValidationResult<Fail>
}

@Service
class PeriodServiceImpl(private val periodRepository: PeriodRepository) : PeriodService {

    override fun startQualificationPeriod(params: StartQualificationPeriodParams): Result<StartQualificationPeriodResult, Fail> {
//      FR.COM-7.10.1
        val periodEntity = PeriodEntity(cpid = params.cpid, ocid = params.ocid, startDate = params.date)

//      FR.COM-7.10.2
        periodRepository.saveNewPeriod(periodEntity)
            .orForwardFail { fail -> return fail }

//      FR.COM-7.10.3
        return StartQualificationPeriodResult(startDate = params.date)
            .asSuccess()
    }

    override fun checkQualificationPeriod(params: CheckQualificationPeriodParams): ValidationResult<Fail> {
        val periodEntity = periodRepository.findBy(cpid = params.cpid, ocid = params.ocid)
            .doReturn { error -> return ValidationResult.error(error) }
            ?: return ValidationError.PeriodNotFoundFor.CheckQualificationPeriod(cpid = params.cpid, ocid = params.ocid)
                .asValidationFailure()

        if (!params.date.isAfter(periodEntity.startDate))
            return ValidationError.RequestDateIsNotAfterStartDate(
                requestDate = params.date, startDate = periodEntity.startDate
            ).asValidationFailure()

        if (!params.date.isBefore(periodEntity.endDate))
            return ValidationError.RequestDateIsNotBeforeEndDate(
                requestDate = params.date, endDate = periodEntity.endDate!!
            ).asValidationFailure()

        return ValidationResult.ok()
    }
}
