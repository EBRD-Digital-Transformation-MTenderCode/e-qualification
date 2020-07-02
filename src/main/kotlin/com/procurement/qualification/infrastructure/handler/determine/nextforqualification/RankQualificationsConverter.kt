package com.procurement.qualification.infrastructure.handler.determine.nextforqualification

import com.procurement.qualification.application.model.params.RankQualificationsParams
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.infrastructure.fail.error.DataErrors

fun RankQualificationsRequest.convert(): Result<RankQualificationsParams, DataErrors> =
    RankQualificationsParams.tryCreate(
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

fun RankQualificationsRequest.Tender.convert(): Result<RankQualificationsParams.Tender, DataErrors> =
    RankQualificationsParams.Tender.tryCreate(
        criteria = this.criteria
            ?.map {
                it.convert()
                    .orForwardFail { fail -> return fail }
            },
        otherCriteria = this.otherCriteria
            .convert()
            .orForwardFail { fail -> return fail }
    )

fun RankQualificationsRequest.Tender.Criteria.convert(): Result<RankQualificationsParams.Tender.Criteria, DataErrors> =
    RankQualificationsParams.Tender.Criteria.tryCreate(
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

fun RankQualificationsRequest.Tender.Criteria.RequirementGroup.convert(): Result<RankQualificationsParams.Tender.Criteria.RequirementGroup, DataErrors> =
    RankQualificationsParams.Tender.Criteria.RequirementGroup.tryCreate(
        id = this.id,
        description = this.description,
        requirements = this.requirements
            .map {
                it.convert()
                    .orForwardFail { fail -> return fail }
            }
    )

fun RankQualificationsRequest.Tender.Criteria.RequirementGroup.Requirement.convert(): Result<RankQualificationsParams.Tender.Criteria.RequirementGroup.Requirement, DataErrors> =
    RankQualificationsParams.Tender.Criteria.RequirementGroup.Requirement.tryCreate(
        id = this.id,
        description = this.description,
        title = this.title,
        dataType = this.dataType
    )

fun RankQualificationsRequest.Tender.OtherCriteria.convert(): Result<RankQualificationsParams.Tender.OtherCriteria, DataErrors> =
    RankQualificationsParams.Tender.OtherCriteria.tryCreate(
        reductionCriteria = this.reductionCriteria,
        qualificationSystemMethods = this.qualificationSystemMethods
    )

fun RankQualificationsRequest.Submission.convert(): Result<RankQualificationsParams.Submission, DataErrors> =
    RankQualificationsParams.Submission.tryCreate(id = this.id, date = this.date)

