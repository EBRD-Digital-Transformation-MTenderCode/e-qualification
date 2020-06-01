package com.procurement.qualification.infrastructure.handler.find.qualificationids

import com.procurement.qualification.application.model.params.FindQualificationIdsParams
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.infrastructure.fail.error.DataErrors

fun FindQualificationIdsRequest.convert(): Result<FindQualificationIdsParams, DataErrors> =
    FindQualificationIdsParams.tryCreate(
        cpid = this.cpid,
        ocid = this.ocid,
        states = this.states
            ?.map {
                it.convert()
                    .orForwardFail { fail -> return fail }
            }
    )

fun FindQualificationIdsRequest.State.convert(): Result<FindQualificationIdsParams.State, DataErrors> =
    FindQualificationIdsParams.State.tryCreate(status = status, statusDetails = statusDetails)
