package com.procurement.qualification.infrastructure.handler.set

import com.procurement.qualification.application.model.params.SetQualificationPeriodEndParams
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.infrastructure.fail.error.DataErrors

fun SetQualificationPeriodEndRequest.convert(): Result<SetQualificationPeriodEndParams, DataErrors> =
    SetQualificationPeriodEndParams.tryCreate(cpid = this.cpid, ocid = this.ocid, date = this.date)
