package com.procurement.qualification.infrastructure.handler.create.qualification

import com.procurement.qualification.application.model.params.DoQualificationParams
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.model.qualification.Qualification
import com.procurement.qualification.infrastructure.fail.error.DataErrors

fun DoQualificationRequest.convert(): Result<DoQualificationParams, DataErrors> =
    DoQualificationParams.tryCreate(
        cpid = this.cpid,
        ocid = this.ocid,
        date = this.date,
        qualifications = this.qualifications
            .map {
                it.convert()
                    .orForwardFail { fail -> return fail }
            }
    )

fun DoQualificationRequest.Qualification.convert(): Result<DoQualificationParams.Qualification, DataErrors> =
    DoQualificationParams.Qualification.tryCreate(
        id = this.id,
        description = this.description,
        statusDetails = this.statusDetails,
        internalId = this.internalId,
        documents = this.documents
            ?.map {
                it.convert()
                    .orForwardFail { fail -> return fail }
            }
    )

fun DoQualificationRequest.Qualification.Document.convert(): Result<DoQualificationParams.Qualification.Document, DataErrors> =
    DoQualificationParams.Qualification.Document.tryCreate(
        id = this.id,
        description = this.description,
        documentType = this.documentType,
        title = this.title
    )

fun Qualification.convertToDoQualificationResult() =
    DoQualificationResult.Qualification(
        id = this.id,
        internalId = this.internalId,
        statusDetails = this.statusDetails,
        relatedSubmission = this.relatedSubmission,
        status = this.status,
        description = this.description,
        date = this.date,
        scoring = this.scoring,
        requirementResponses = this.requirementResponses
            .map { requirementResponse ->
                DoQualificationResult.Qualification.RequirementResponse(
                    id = requirementResponse.id,
                    value = requirementResponse.value,
                    relatedTenderer = requirementResponse.relatedTenderer
                        .let { DoQualificationResult.Qualification.RequirementResponse.RelatedTenderer(id = it.id) },
                    requirement = requirementResponse.requirement
                        .let { DoQualificationResult.Qualification.RequirementResponse.Requirement(id = it.id) },
                    responder = requirementResponse.responder
                        .let {
                            DoQualificationResult.Qualification.RequirementResponse.Responder(
                                id = it.id,
                                name = it.name
                            )
                        }
                )
            },
        documents = this.documents
            .map {
                DoQualificationResult.Qualification.Document(
                    id = it.id,
                    description = it.description,
                    documentType = it.documentType,
                    title = it.title
                )
            }
    )

