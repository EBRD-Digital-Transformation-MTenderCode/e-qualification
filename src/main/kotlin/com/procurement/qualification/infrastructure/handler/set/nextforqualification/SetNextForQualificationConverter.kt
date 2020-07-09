package com.procurement.qualification.infrastructure.handler.set.nextforqualification

import com.procurement.qualification.application.model.params.SetNextForQualificationParams
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.model.qualification.Qualification
import com.procurement.qualification.infrastructure.fail.error.DataErrors

fun SetNextForQualificationRequest.convert(): Result<SetNextForQualificationParams, DataErrors> =
    SetNextForQualificationParams.tryCreate(
        cpid = this.cpid,
        ocid = this.ocid,
        tender = this.tender
            .convert()
            .orForwardFail { fail -> return fail },
        submissions = this.submissions
            .map {
                it.convert()
                    .orForwardFail { fail -> return fail }
            },
        criteria = this.criteria
            ?.map {
                it.convert()
                    .orForwardFail { fail -> return fail }
            }

    )

fun SetNextForQualificationRequest.Tender.convert(): Result<SetNextForQualificationParams.Tender, DataErrors> =
    SetNextForQualificationParams.Tender.tryCreate(
        otherCriteria = this.otherCriteria
            .convert()
            .orForwardFail { fail -> return fail }
    )

fun SetNextForQualificationRequest.Tender.OtherCriteria.convert(): Result<SetNextForQualificationParams.Tender.OtherCriteria, DataErrors> =
    SetNextForQualificationParams.Tender.OtherCriteria.tryCreate(
        qualificationSystemMethods = this.qualificationSystemMethods,
        reductionCriteria = this.reductionCriteria
    )

fun SetNextForQualificationRequest.Submission.convert(): Result<SetNextForQualificationParams.Submission, DataErrors> =
    SetNextForQualificationParams.Submission.tryCreate(id = this.id, date = this.date)

fun SetNextForQualificationRequest.Criteria.convert(): Result<SetNextForQualificationParams.Criteria, DataErrors> =
    SetNextForQualificationParams.Criteria.tryCreate(
        id = this.id,
        description = this.description,
        title = this.title,
        source = this.source,
        relatedItem = this.relatedItem,
        relatesTo = this.relatesTo,
        requirementGroups = this.requirementGroups
            .map {
                it.convert()
                    .orForwardFail { fail -> return fail }
            }
    )

fun SetNextForQualificationRequest.Criteria.RequirementGroup.convert(): Result<SetNextForQualificationParams.Criteria.RequirementGroup, DataErrors> =
    SetNextForQualificationParams.Criteria.RequirementGroup.tryCreate(
        id = this.id,
        descriptions = this.descriptions,
        requirements = this.requirements
            .map {
                it.convert()
                    .orForwardFail { fail -> return fail }
            }
    )

fun SetNextForQualificationRequest.Criteria.RequirementGroup.Requirement.convert(): Result<SetNextForQualificationParams.Criteria.RequirementGroup.Requirement, DataErrors> =
    SetNextForQualificationParams.Criteria.RequirementGroup.Requirement.tryCreate(
        id = this.id,
        title = this.title,
        description = this.description,
        dataType = this.dataType
    )


fun Qualification.convertToSetNextForQualification(): SetNextForQualificationResult.Qualification =
    SetNextForQualificationResult.Qualification(
        id = this.id,
        date = this.date,
        status = this.status,
        relatedSubmission = this.relatedSubmission,
        scoring = this.scoring,
        internalId = this.internalId,
        statusDetails = this.statusDetails,
        documents = this.documents
            .map {
                SetNextForQualificationResult.Qualification.Document(
                    id = it.id,
                    description = it.description,
                    title = it.title,
                    documentType = it.documentType
                )
            },
        requirementResponses = this.requirementResponses
            .map { requirementResponse ->
                SetNextForQualificationResult.Qualification.RequirementResponse(
                    id = requirementResponse.id,
                    value = requirementResponse.value,
                    requirement = requirementResponse.requirement
                        .let {
                            SetNextForQualificationResult.Qualification.RequirementResponse.Requirement(
                                id = it.id
                            )
                        },
                    relatedTenderer = requirementResponse.relatedTenderer
                        .let {
                            SetNextForQualificationResult.Qualification.RequirementResponse.RelatedTenderer(
                                id = it.id
                            )
                        },
                    responder = requirementResponse.responder
                        .let {
                            SetNextForQualificationResult.Qualification.RequirementResponse.Responder(
                                id = it.id,
                                name = it.name
                            )
                        }
                )
            }
    )
