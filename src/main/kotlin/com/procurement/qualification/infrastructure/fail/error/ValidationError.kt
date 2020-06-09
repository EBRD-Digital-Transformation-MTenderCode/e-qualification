package com.procurement.qualification.infrastructure.fail.error

import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.domain.model.qualification.QualificationId
import com.procurement.qualification.infrastructure.fail.Fail

sealed class ValidationError(
    numberError: String,
    override val description: String,
    val entityId: String? = null
) : Fail.Error("VR.COM-") {
    override val code: String = prefix + numberError

    class QualificationNotFoundOnGetNextsForQualification(cpid: Cpid, ocid: Ocid, id: QualificationId) :
        ValidationError(
            numberError = "7.13.1",
            description = "Qualifications with id=${id} not found by cpid=${cpid} and ocid=${ocid}."
        )
}