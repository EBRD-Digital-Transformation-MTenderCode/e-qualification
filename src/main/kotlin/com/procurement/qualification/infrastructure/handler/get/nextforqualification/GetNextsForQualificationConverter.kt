package com.procurement.qualification.infrastructure.handler.get.nextforqualification

import com.procurement.qualification.application.model.params.GetNextsForQualificationParams
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.infrastructure.fail.error.DataErrors

fun GetNextsForQualificationRequest.convert(): Result<GetNextsForQualificationParams, DataErrors> =
    GetNextsForQualificationParams.tryCreate(
        cpid = this.cpid,
        ocid = this.ocid,
        reductionCriteria = this.reductionCriteria,
        qualificationSystemMethods = this.qualificationSystemMethods,
        submissions = this.submissions
            .map {
                it.convert()
                    .orForwardFail { fail -> return fail }
            },
        qualifications = this.qualifications
            .map {
                it.convert()
                    .orForwardFail { fail -> return fail }
            }
    )

fun GetNextsForQualificationRequest.Qualification.convert(): Result<GetNextsForQualificationParams.Qualification, DataErrors> =
    GetNextsForQualificationParams.Qualification.tryCreate(
        id = this.id,
        date = this.date,
        scoring = this.scoring,
        relatedSubmission = this.relatedSubmission
    )

fun GetNextsForQualificationRequest.Submission.convert(): Result<GetNextsForQualificationParams.Submission, DataErrors> =
    GetNextsForQualificationParams.Submission.tryCreate(id = this.id, date = this.date)

