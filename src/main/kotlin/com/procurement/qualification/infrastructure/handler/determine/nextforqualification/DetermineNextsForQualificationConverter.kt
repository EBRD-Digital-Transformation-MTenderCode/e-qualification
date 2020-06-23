package com.procurement.qualification.infrastructure.handler.determine.nextforqualification

import com.procurement.qualification.application.model.params.DetermineNextsForQualificationParams
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.infrastructure.fail.error.DataErrors

fun DetermineNextsForQualificationRequest.convert(): Result<DetermineNextsForQualificationParams, DataErrors> =
    DetermineNextsForQualificationParams.tryCreate(
        cpid = this.cpid,
        ocid = this.ocid,
        tender = this.tender
            .convert()
            .orForwardFail { fail -> return fail },
        submissions = this.submissions
            .map {
                it.convert()
                    .orForwardFail { fail -> return fail }
            }
    )

fun DetermineNextsForQualificationRequest.Tender.convert(): Result<DetermineNextsForQualificationParams.Tender, DataErrors> =
    DetermineNextsForQualificationParams.Tender.tryCreate(
        criteria = this.criteria
            ?.map {
                it.convert()
                    .orForwardFail { fail -> return fail }
            },
        otherCriteria = this.otherCriteria
            .convert()
            .orForwardFail { fail -> return fail }
    )

fun DetermineNextsForQualificationRequest.Tender.Criteria.convert(): Result<DetermineNextsForQualificationParams.Tender.Criteria, DataErrors> =
    DetermineNextsForQualificationParams.Tender.Criteria.tryCreate(
        id = this.id,
        title = this.title,
        description = this.description,
        relatedItem = this.relatedItem,
        relatesTo = this.relatesTo,
        source = this.source,
        requirementGroups = this.requirementGroups
            .map {
                it.convert()
                    .orForwardFail { fail -> return fail }
            }

    )

fun DetermineNextsForQualificationRequest.Tender.Criteria.RequirementGroup.convert(): Result<DetermineNextsForQualificationParams.Tender.Criteria.RequirementGroup, DataErrors> =
    DetermineNextsForQualificationParams.Tender.Criteria.RequirementGroup.tryCreate(
        id = this.id,
        description = this.description,
        requirements = this.requirements
            .map {
                it.convert()
                    .orForwardFail { fail -> return fail }
            }
    )

fun DetermineNextsForQualificationRequest.Tender.Criteria.RequirementGroup.Requirement.convert(): Result<DetermineNextsForQualificationParams.Tender.Criteria.RequirementGroup.Requirement, DataErrors> =
    DetermineNextsForQualificationParams.Tender.Criteria.RequirementGroup.Requirement.tryCreate(
        id = this.id,
        description = this.description,
        title = this.title,
        dataType = this.dataType
    )

fun DetermineNextsForQualificationRequest.Tender.OtherCriteria.convert(): Result<DetermineNextsForQualificationParams.Tender.OtherCriteria, DataErrors> =
    DetermineNextsForQualificationParams.Tender.OtherCriteria.tryCreate(
        reductionCriteria = this.reductionCriteria,
        qualificationSystemMethods = this.qualificationSystemMethods
    )

fun DetermineNextsForQualificationRequest.Submission.convert(): Result<DetermineNextsForQualificationParams.Submission, DataErrors> =
    DetermineNextsForQualificationParams.Submission.tryCreate(id = this.id, date = this.date)

