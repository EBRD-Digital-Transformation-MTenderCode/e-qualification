package com.procurement.qualification.infrastructure.fail.error

import com.procurement.qualification.domain.enums.OperationType
import com.procurement.qualification.domain.enums.Pmd
import com.procurement.qualification.domain.enums.RequirementDataType
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.domain.model.Owner
import com.procurement.qualification.domain.model.Token
import com.procurement.qualification.domain.model.qualification.QualificationId
import com.procurement.qualification.domain.model.requirement.RequirementId
import com.procurement.qualification.domain.model.requirement.RequirementResponseValue
import com.procurement.qualification.domain.model.requirementresponse.RequirementResponseId
import com.procurement.qualification.domain.model.submission.SubmissionId
import com.procurement.qualification.infrastructure.fail.Fail

sealed class ValidationError(
    numberError: String,
    override val description: String,
    val entityId: String? = null
) : Fail.Error("VR.COM-") {
    override val code: String = prefix + numberError

    class QualificationsNotFoundOnRankQualifications(cpid: Cpid, ocid: Ocid) :
        ValidationError(
            numberError = "7.13.1",
            description = "Qualifications not found by cpid=${cpid} and ocid=${ocid}."
        )

    class RelatedSubmissionNotEqualOnRankQualifications(submissionId: SubmissionId) :
        ValidationError(
            numberError = "7.13.2",
            description = "Related submission in qualifications not found on submission id='$submissionId'."
        )

    class InvalidTokenOnCheckAccessToQualification(token: Token, cpid: Cpid) : ValidationError(
        numberError = "7.14.1",
        description = "Invalid token '$token' by cpid '$cpid'."
    )

    class InvalidOwnerOnCheckAccessToQualification(owner: Owner, cpid: Cpid) : ValidationError(
        numberError = "7.14.2",
        description = "Invalid owner '$owner' by cpid '$cpid'."
    )

    class QualificationStatesNotFound(
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

    class RequirementNotFoundOnCheckDeclaration(requirementId: RequirementId) :
        ValidationError(
            numberError = "7.16.2",
            description = "Requirement with id='$requirementId' not found."
        )

    class ValueDataTypeMismatchOnCheckDeclaration(actual: RequirementResponseValue, expected: RequirementDataType) :
        ValidationError(
            numberError = "7.16.3",
            description = "Requirement datatype mismatch, expected='$expected' , actual='$actual'."
        )

    class InvalidRequirementResponseIdOnCheckDeclaration(
        actualId: RequirementResponseId,
        expected: RequirementResponseId
    ) :
        ValidationError(
            numberError = "7.16.4",
            description = "Invalid Requirement Response Id, actual='$actualId', expected='$expected'."
        )

    sealed class QualificationNotFoundFor(
        numberError: String,
        cpid: Cpid,
        ocid: Ocid,
        qualificationId: QualificationId
    ) : ValidationError(
        numberError = numberError,
        description = "Qualification not found by cpid='$cpid' and ocid='$ocid' and id='$qualificationId'."
    ) {
        class CheckAccessToQualification(cpid: Cpid, ocid: Ocid, qualificationId: QualificationId) :
            QualificationNotFoundFor(
                numberError = "7.14.3", cpid = cpid, ocid = ocid, qualificationId = qualificationId
            )

        class CheckQualificationState(cpid: Cpid, ocid: Ocid, qualificationId: QualificationId) :
            QualificationNotFoundFor(
                numberError = "7.17.1", cpid = cpid, ocid = ocid, qualificationId = qualificationId
            )

        class DoDeclaration(cpid: Cpid, ocid: Ocid, qualificationId: QualificationId) :
            QualificationNotFoundFor(
                numberError = "7.19.1", cpid = cpid, ocid = ocid, qualificationId = qualificationId
            )

        class CheckDeclaration(cpid: Cpid, ocid: Ocid, qualificationId: QualificationId) :
            QualificationNotFoundFor(
                numberError = "7.16.1", cpid = cpid, ocid = ocid, qualificationId = qualificationId
            )

        class FindRequirementResponseByIds(cpid: Cpid, ocid: Ocid, qualificationId: QualificationId) :
            QualificationNotFoundFor(
                numberError = "7.18.1", cpid = cpid, ocid = ocid, qualificationId = qualificationId
            )

        class DoConsideration(cpid: Cpid, ocid: Ocid, qualificationId: QualificationId) :
            QualificationNotFoundFor(
                numberError = "7.21.1", cpid = cpid, ocid = ocid, qualificationId = qualificationId
            )


    }
    class QualificationNotFoundOnFindRequirementResponseByIds(
        cpid: Cpid,
        ocid: Ocid,
        qualificationId: QualificationId
    ) :
        ValidationError(
            numberError = "7.18.1",
            description = "Qualification not found by cpid='$cpid' and ocid='$ocid' and id='$qualificationId'."
        )

    class RelatedSubmissionNotEqualOnSetNextForQualification(submissionId: SubmissionId) :
        ValidationError(
            numberError = "7.22.1",
            description = "Related submission in qualifications not found on submission id='$submissionId'."
        )

    class QualificationNotFoundOnDoQualification(cpid: Cpid, ocid: Ocid, qualificationId: QualificationId) :
        ValidationError(
            numberError = "7.20.1",
            description = "Qualification not found by cpid='$cpid' and ocid='$ocid' and id='$qualificationId'.",
            entityId = qualificationId.toString()
        )
}