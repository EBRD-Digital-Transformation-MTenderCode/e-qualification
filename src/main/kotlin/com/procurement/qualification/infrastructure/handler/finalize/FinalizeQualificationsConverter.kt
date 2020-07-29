package com.procurement.qualification.infrastructure.handler.finalize

import com.procurement.qualification.application.model.params.FinalizeQualificationsParams
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.infrastructure.fail.error.DataErrors

fun FinalizeQualificationsRequest.convert(): Result<FinalizeQualificationsParams, DataErrors> =
    FinalizeQualificationsParams.tryCreate(cpid = cpid, ocid = ocid)
