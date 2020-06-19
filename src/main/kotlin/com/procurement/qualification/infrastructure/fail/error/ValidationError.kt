package com.procurement.qualification.infrastructure.fail.error

import com.procurement.qualification.domain.enums.OperationType
import com.procurement.qualification.domain.enums.Pmd
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.domain.model.Owner
import com.procurement.qualification.domain.model.Token
import com.procurement.qualification.domain.model.qualification.QualificationId
import com.procurement.qualification.infrastructure.fail.Fail

sealed class ValidationError(
    numberError: String,
    override val description: String,
    val entityId: String? = null
) : Fail.Error("VR.COM-") {
    override val code: String = prefix + numberError

    class InvalidTokenOnCheckAccessToQualification(token: Token, cpid: Cpid) : ValidationError(
        numberError = "7.14.1",
        description = "Invalid token '$token' by cpid '$cpid'."
    )

    class InvalidOwnerOnCheckAccessToQualification(owner: Owner, cpid: Cpid) : ValidationError(
        numberError = "7.14.2",
        description = "Invalid owner '$owner' by cpid '$cpid'."
    )

    class QualificationNotFoundByCheckAccessToQualification(cpid: Cpid, ocid: Ocid, qualificationId: QualificationId) :
        ValidationError(
            numberError = "7.14.3",
            description = "Qualification not found by cpid='$cpid' and ocid='$ocid' and id='$qualificationId'."
        )

    class QualificationNotFoundByCheckQualificationState(cpid: Cpid, ocid: Ocid, qualificationId: QualificationId) :
        ValidationError(
            numberError = "7.17.1",
            description = "Qualification not found by cpid='$cpid' and ocid='$ocid' and id='$qualificationId'."
        )

    class QualificationStatesNotFoundOnCheckQualificationState(
        country: String,
        pmd: Pmd,
        operationType: OperationType
    ) : ValidationError(
        numberError = "17",
        description = "Qualification states not found by country='$country' and pmd='$pmd' and operationType='$operationType'."
    )

    class QualificationStatesIsInvalidOnCheckQualificationState(qualificationId: QualificationId) : ValidationError(
        numberError = "7.17.2",
        description = "Qualification with id='$qualificationId' has invalid states."
    )
}