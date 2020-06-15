package com.procurement.qualification.infrastructure.fail.error

import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.domain.model.submission.SubmissionId
import com.procurement.qualification.infrastructure.fail.Fail

sealed class ValidationError(
    numberError: String,
    override val description: String,
    val entityId: String? = null
) : Fail.Error("VR.COM-") {
    override val code: String = prefix + numberError

    class QualificationsNotFoundOnDetermineNextsForQualification(cpid: Cpid, ocid: Ocid) :
        ValidationError(
            numberError = "7.13.1",
            description = "Qualifications not found by cpid=${cpid} and ocid=${ocid}."
        )

    class RelatedSubmissionNotEqualOnDetermineNextsForQualification(submissionId: SubmissionId) :
        ValidationError(
            numberError = "7.13.2",
            description = "Related submission in qualifications not found on submission id='$submissionId'."
        )
}