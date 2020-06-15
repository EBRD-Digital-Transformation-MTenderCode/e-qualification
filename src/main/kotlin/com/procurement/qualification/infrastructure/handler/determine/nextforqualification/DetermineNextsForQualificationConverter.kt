package com.procurement.qualification.infrastructure.handler.determine.nextforqualification

import com.procurement.qualification.application.model.params.DetermineNextsForQualificationParams
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.infrastructure.fail.error.DataErrors

fun DetermineNextsForQualificationRequest.convert(): Result<DetermineNextsForQualificationParams, DataErrors> =
    DetermineNextsForQualificationParams.tryCreate(
        cpid = this.cpid,
        ocid = this.ocid,
        otherCriteria = this.otherCriteria
            .let {
                it.convert()
                    .orForwardFail { fail -> return fail }
            },
        submissions = this.submissions
            .map {
                it.convert()
                    .orForwardFail { fail -> return fail }
            }
    )

fun DetermineNextsForQualificationRequest.OtherCriteria.convert(): Result<DetermineNextsForQualificationParams.OtherCriteria, DataErrors> =
    DetermineNextsForQualificationParams.OtherCriteria.tryCreate(
        reductionCriteria = this.reductionCriteria,
        qualificationSystemMethods = this.qualificationSystemMethods
    )

fun DetermineNextsForQualificationRequest.Submission.convert(): Result<DetermineNextsForQualificationParams.Submission, DataErrors> =
    DetermineNextsForQualificationParams.Submission.tryCreate(id = this.id, date = this.date)

