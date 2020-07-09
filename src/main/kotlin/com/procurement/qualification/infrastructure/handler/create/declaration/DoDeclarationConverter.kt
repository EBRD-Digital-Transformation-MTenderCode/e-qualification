package com.procurement.qualification.infrastructure.handler.create.declaration

import com.procurement.qualification.application.model.params.DoDeclarationParams
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.model.qualification.Qualification
import com.procurement.qualification.infrastructure.fail.error.DataErrors

fun DoDeclarationRequest.convert(): Result<DoDeclarationParams, DataErrors> =
    DoDeclarationParams.tryCreate(
        cpid = this.cpid,
        ocid = this.ocid,
        qualifications = this.qualifications
            .map {
                it.convert()
                    .orForwardFail { fail -> return fail }
            }
    )

fun DoDeclarationRequest.Qualification.convert(): Result<DoDeclarationParams.Qualification, DataErrors> =
    DoDeclarationParams.Qualification.tryCreate(
        id = this.id,
        requirementResponses = this.requirementResponses
            .map {
                it.convert()
                    .orForwardFail { fail -> return fail }
            }
    )

fun DoDeclarationRequest.Qualification.RequirementResponse.convert(): Result<DoDeclarationParams.Qualification.RequirementResponse, DataErrors> =
    DoDeclarationParams.Qualification.RequirementResponse.tryCreate(
        id = this.id,
        value = this.value,
        responder = this.responder
            .convert()
            .orForwardFail { fail -> return fail },
        requirement = this.requirement
            .convert()
            .orForwardFail { fail -> return fail },
        relatedTenderer = this.relatedTenderer
            .convert()
            .orForwardFail { fail -> return fail }
    )

fun DoDeclarationRequest.Qualification.RequirementResponse.RelatedTenderer.convert(): Result<DoDeclarationParams.Qualification.RequirementResponse.RelatedTenderer, DataErrors> =
    DoDeclarationParams.Qualification.RequirementResponse.RelatedTenderer.tryCreate(id = this.id)

fun DoDeclarationRequest.Qualification.RequirementResponse.Requirement.convert(): Result<DoDeclarationParams.Qualification.RequirementResponse.Requirement, DataErrors> =
    DoDeclarationParams.Qualification.RequirementResponse.Requirement.tryCreate(id = this.id)

fun DoDeclarationRequest.Qualification.RequirementResponse.Responder.convert(): Result<DoDeclarationParams.Qualification.RequirementResponse.Responder, DataErrors> =
    DoDeclarationParams.Qualification.RequirementResponse.Responder.tryCreate(id = this.id, name = this.name)

fun List<Qualification>.convertQualificationsToDoDeclarationResult(): DoDeclarationResult =
    DoDeclarationResult(
        qualifications = this.map { qualification ->
            DoDeclarationResult.Qualification(
                id = qualification.id,
                requirementResponses = qualification.requirementResponses
                    .map { rr ->
                        DoDeclarationResult.Qualification.RequirementResponse(
                            id = rr.id,
                            value = rr.value,
                            relatedTenderer = rr.relatedTenderer
                                .let { DoDeclarationResult.Qualification.RequirementResponse.RelatedTenderer(id = it.id) },
                            requirement = rr.requirement
                                .let { DoDeclarationResult.Qualification.RequirementResponse.Requirement(id = it.id) },
                            responder = rr.responder
                                .let {
                                    DoDeclarationResult.Qualification.RequirementResponse.Responder(
                                        id = it.id,
                                        name = it.name
                                    )
                                }
                        )
                    }
            )
        }
    )
