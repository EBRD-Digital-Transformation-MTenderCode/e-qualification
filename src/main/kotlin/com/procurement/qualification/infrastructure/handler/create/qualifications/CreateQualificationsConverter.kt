package com.procurement.qualification.infrastructure.handler.create.qualifications

import com.procurement.qualification.application.model.params.CreateQualificationsParams
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.util.extension.mapResult
import com.procurement.qualification.infrastructure.fail.error.DataErrors

fun CreateQualificationsRequest.convert(): Result<CreateQualificationsParams, DataErrors> =
    CreateQualificationsParams.tryCreate(
        cpid = this.cpid,
        ocid = this.ocid,
        submissions = this.submissions
            .map {
                it.convert()
                    .orForwardFail { fail -> return fail }
            },
        owner = this.owner,
        date = this.date,
        tender = this.tender.convert()
            .orForwardFail { fail -> return fail }
    )

fun CreateQualificationsRequest.Submission.convert(): Result<CreateQualificationsParams.Submission, DataErrors> =
    CreateQualificationsParams.Submission.tryCreate(
        id = this.id,
        requirementResponses = this.requirementResponses
            ?.map {
                it.convert()
                    .orForwardFail { fail -> return fail }
            }
            .orEmpty()
    )

fun CreateQualificationsRequest.Submission.RequirementResponse.convert(): Result<CreateQualificationsParams.Submission.RequirementResponse, DataErrors> =
    CreateQualificationsParams.Submission.RequirementResponse.tryCreate(
        id = this.id,
        value = this.value,
        relatedCandidate = this.relatedCandidate.convert()
            .orForwardFail { fail -> return fail },
        requirement = this.requirement.convert()
            .orForwardFail { fail -> return fail }
    )

fun CreateQualificationsRequest.Submission.RequirementResponse.RelatedCandidate.convert(): Result<CreateQualificationsParams.Submission.RequirementResponse.RelatedCandidate, DataErrors> =
    CreateQualificationsParams.Submission.RequirementResponse.RelatedCandidate.tryCreate(id = this.id, name = this.name)

fun CreateQualificationsRequest.Submission.RequirementResponse.Requirement.convert(): Result<CreateQualificationsParams.Submission.RequirementResponse.Requirement, DataErrors> =
    CreateQualificationsParams.Submission.RequirementResponse.Requirement.tryCreate(id = this.id)

fun CreateQualificationsRequest.Tender.convert(): Result<CreateQualificationsParams.Tender, DataErrors> =
    CreateQualificationsParams.Tender.tryCreate(
        conversions = this.conversions
            ?.map {
                it.convert()
                    .orForwardFail { fail -> return fail }
            },
        otherCriteria = this.otherCriteria.convert()
            .orForwardFail { fail -> return fail },
        criteria = criteria?.mapResult { it.convert() }
            ?.orForwardFail { fail -> return fail }
    )

private fun CreateQualificationsRequest.Tender.Criterion.convert(): Result<CreateQualificationsParams.Tender.Criterion, DataErrors> =
    CreateQualificationsParams.Tender.Criterion.tryCreate(
        id = id,
        relatesTo = relatesTo,
        source = source,
        classification = CreateQualificationsParams.Tender.Criterion.Classification(
            id = classification.id,
            scheme = classification.scheme
        ),
        requirementGroups = requirementGroups.mapResult { it.convert() }
            .orForwardFail { fail -> return fail }
    )

private fun CreateQualificationsRequest.Tender.Criterion.RequirementGroup.convert(): Result<CreateQualificationsParams.Tender.Criterion.RequirementGroup, DataErrors> =
    CreateQualificationsParams.Tender.Criterion.RequirementGroup.tryCreate(
        id = id,
        requirements = requirements.mapResult { it.convert() }
            .orForwardFail { fail -> return fail }
    )

private fun CreateQualificationsRequest.Tender.Criterion.RequirementGroup.Requirement.convert(): Result<CreateQualificationsParams.Tender.Criterion.RequirementGroup.Requirement, DataErrors> =
    CreateQualificationsParams.Tender.Criterion.RequirementGroup.Requirement.tryCreate(
        id = id,
        dataType = dataType,
        status = status
    )

fun CreateQualificationsRequest.Tender.OtherCriteria.convert(): Result<CreateQualificationsParams.Tender.OtherCriteria, DataErrors> =
    CreateQualificationsParams.Tender.OtherCriteria.tryCreate(
        reductionCriteria = this.reductionCriteria,
        qualificationSystemMethods = this.qualificationSystemMethods
    )

fun CreateQualificationsRequest.Tender.Conversion.convert(): Result<CreateQualificationsParams.Tender.Conversion, DataErrors> =
    CreateQualificationsParams.Tender.Conversion.tryCreate(
        id = this.id,
        relatesTo = this.relatesTo,
        description = this.description,
        coefficients = this.coefficients
            .map {
                it.convert()
                    .orForwardFail { fail -> return fail }
            },
        rationale = this.rationale,
        relatedItem = this.relatedItem
    )

fun CreateQualificationsRequest.Tender.Conversion.Coefficient.convert(): Result<CreateQualificationsParams.Tender.Conversion.Coefficient, DataErrors> =
    CreateQualificationsParams.Tender.Conversion.Coefficient.tryCreate(
        id = this.id,
        value = this.value,
        coefficient = this.coefficient
    )
