package com.procurement.qualification.application.service

import com.procurement.qualification.application.model.params.StartQualificationPeriodParams
import com.procurement.qualification.application.repository.PeriodRepository
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.handler.start.qualificationperiod.StartQualificationPeriodResult
import com.procurement.qualification.infrastructure.model.entity.PeriodEntity
import org.springframework.stereotype.Service

interface PeriodService {

    fun startQualificationPeriod(params: StartQualificationPeriodParams): Result<StartQualificationPeriodResult, Fail>
}

@Service
class PeriodServiceImpl(val periodRepository: PeriodRepository) : PeriodService {

    override fun startQualificationPeriod(params: StartQualificationPeriodParams): Result<StartQualificationPeriodResult, Fail> {
//      FR.COM-7.10.1
        val periodEntity = PeriodEntity(cpid = params.cpid, ocid = params.ocid, startDate = params.date)

//      FR.COM-7.10.2
        periodRepository.saveNewPeriod(periodEntity)
            .orForwardFail { fail-> return fail }

//      FR.COM-7.10.3
        return StartQualificationPeriodResult(startDate = params.date)
            .asSuccess()
    }
}
