package com.procurement.qualification.infrastructure.handler.create.consideration

import com.procurement.qualification.application.model.params.DoConsiderationParams
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.util.extension.mapResult
import com.procurement.qualification.infrastructure.fail.error.DataErrors

fun DoConsiderationRequest.convert(): Result<DoConsiderationParams, DataErrors> =
    DoConsiderationParams.tryCreate(
        cpid = cpid,
        ocid = ocid,
        qualifications = qualifications.mapResult { it.convert() }.orForwardFail { fail -> return fail }
    )

fun DoConsiderationRequest.Qualification.convert(): Result<DoConsiderationParams.Qualification, DataErrors> =
    DoConsiderationParams.Qualification.tryCreate(id = id)


