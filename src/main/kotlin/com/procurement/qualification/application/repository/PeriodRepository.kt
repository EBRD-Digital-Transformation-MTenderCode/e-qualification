package com.procurement.qualification.application.repository

import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.model.entity.PeriodEntity

interface PeriodRepository {
    fun findBy(cpid: Cpid, ocid: Ocid): Result<PeriodEntity?, Fail.Incident>
    fun saveNewPeriod(period: PeriodEntity): Result<Boolean, Fail.Incident>
}
