package com.procurement.qualification.infrastructure.handler.check.accesstoqualification

import com.procurement.qualification.application.model.params.CheckAccessToQualificationParams
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.infrastructure.fail.error.DataErrors

fun CheckAccessToQualificationRequest.convert(): Result<CheckAccessToQualificationParams, DataErrors> =
    CheckAccessToQualificationParams.tryCreate(
        cpid = this.cpid,
        ocid = this.ocid,
        owner = this.owner,
        token = this.token,
        qualificationId = this.qualificationId
    )

