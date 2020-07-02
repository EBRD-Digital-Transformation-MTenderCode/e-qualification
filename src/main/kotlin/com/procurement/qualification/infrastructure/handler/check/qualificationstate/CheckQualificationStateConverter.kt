package com.procurement.qualification.infrastructure.handler.check.qualificationstate

import com.procurement.qualification.application.model.params.CheckQualificationStateParams
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.infrastructure.fail.error.DataErrors

fun CheckQualificationStateRequest.convert(): Result<CheckQualificationStateParams, DataErrors> =
    CheckQualificationStateParams.tryCreate(
        cpid = this.cpid,
        ocid = this.ocid,
        operationType = this.operationType,
        country = this.country,
        pmd = this.pmd,
        qualificationId = this.qualificationId
    )
