package com.procurement.qualification.infrastructure.fail.error

import com.procurement.qualification.domain.enums.OperationType
import com.procurement.qualification.domain.enums.ProcurementMethodDetails
import com.procurement.qualification.domain.enums.RequirementDataType
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.domain.model.Owner
import com.procurement.qualification.domain.model.Token
import com.procurement.qualification.domain.model.date.format
import com.procurement.qualification.domain.model.person.PersonId
import com.procurement.qualification.domain.model.qualification.QualificationId
import com.procurement.qualification.domain.model.requirement.RequirementId
import com.procurement.qualification.domain.model.requirement.RequirementResponseValue
import com.procurement.qualification.domain.model.requirementresponse.RequirementResponseId
import com.procurement.qualification.domain.model.submission.SubmissionId
import com.procurement.qualification.infrastructure.fail.Fail
import java.time.LocalDateTime

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
        pmd: ProcurementMethodDetails,
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

    class InvalidResponderNameOnCheckDeclaration(expected: String, actual: String) : ValidationError(
        numberError = "7.16.5",
        description = "Invalid Responder name, actual='$actual', expected='$expected'."
    )

    class ResponderIdMismatchOnCheckDeclaration(expected: PersonId, actual: PersonId) : ValidationError(
        numberError = "7.16.6",
        description = "Invalid Responder name, actual='$actual', expected='$expected'."
    )

    class ResponderNameMismatchOnCheckDeclaration(expected: String, actual: String) : ValidationError(
        numberError = "7.16.7",
        description = "Invalid Responder name, actual='$actual', expected='$expected'."
    )

    sealed class QualificationNotFoundFor : ValidationError {
        constructor(numberError: String, cpid: Cpid, ocid: Ocid, qualificationId: QualificationId) :
            super(numberError, "Qualification not found by cpid='$cpid' and ocid='$ocid' and id='$qualificationId'.")

        constructor(numberError: String, cpid: Cpid, ocid: Ocid) :
            super(numberError, "No qualification found by cpid='$cpid' and ocid='$ocid'.")

        constructor(numberError: String, cpid: Cpid, ocid: Ocid, qualificationIds: Collection<QualificationId>) :
            super(
                numberError = numberError,
                description = "Qualifications not found by cpid='$cpid' and ocid='$ocid' and id='${qualificationIds.joinToString()}'."
            )

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

        class DoQualification(cpid: Cpid, ocid: Ocid, qualificationIds: Collection<QualificationId>) :
            QualificationNotFoundFor(
                numberError = "7.20.1", ocid = ocid, cpid = cpid, qualificationIds = qualificationIds
            )

        class CheckQualificationsForProtocol(cpid: Cpid, ocid: Ocid) :
            QualificationNotFoundFor(numberError = "7.24.1", cpid = cpid, ocid = ocid)

        class FinalizeQualifications(cpid: Cpid, ocid: Ocid) :
            QualificationNotFoundFor(numberError = "7.26.1", cpid = cpid, ocid = ocid)
    }

    sealed class PeriodNotFoundFor(
        numberError: String,
        cpid: Cpid,
        ocid: Ocid
    ) : ValidationError(
        numberError = numberError,
        description = "Period not found by cpid='$cpid' and ocid='$ocid'."
    ) {
        class CheckQualificationPeriod(cpid: Cpid, ocid: Ocid) : PeriodNotFoundFor(
            numberError = "7.4.1", cpid = cpid, ocid = ocid
        )

        class SetQualificationPeriodEnd(cpid: Cpid, ocid: Ocid) : PeriodNotFoundFor(
            numberError = "7.23.1", cpid = cpid, ocid = ocid
        )
    }

    class RelatedSubmissionNotEqualOnSetNextForQualification(submissionId: SubmissionId) :
        ValidationError(
            numberError = "7.22.1",
            description = "Related submission in qualifications not found on submission id='$submissionId'."
        )

    class RequestDateIsNotAfterStartDate(requestDate: LocalDateTime, startDate: LocalDateTime) : ValidationError(
        numberError = "7.4.3",
        description = "Request date '${requestDate.format()}' must be after stored period start date '${startDate.format()}'."
    )

    class UnsuitableQualificationFound(cpid: Cpid, ocid: Ocid, id: QualificationId) : ValidationError(
        numberError = "7.24.2",
        description = "Unsuitable qualification found by cpid '$cpid', ocid '$ocid', id '$id'."
    )

    class RuleNotFound : ValidationError {
        constructor(description: String) :
            super(numberError = "17", description = description)

        constructor(country: String, pmd: ProcurementMethodDetails, operationType: OperationType?) :
            this(description = "Rule not found by country '$country', pmd '$pmd', operationType $operationType.")

        constructor(country: String, pmd: ProcurementMethodDetails) :
            this(description = "Rule not found by country '$country', pmd '$pmd'.")
    }
}