package com.procurement.qualification.infrastructure.handler.find.requirementresponsebyids

import com.procurement.qualification.application.model.params.FindRequirementResponseByIdsParams
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.model.qualification.Qualification
import com.procurement.qualification.infrastructure.fail.Fail

fun FindRequirementResponseByIdsRequest.convert(): Result<FindRequirementResponseByIdsParams, Fail> =
    FindRequirementResponseByIdsParams.tryCreate(requirementResponseIds, qualificationId, cpid, ocid)


fun Qualification.RequirementResponse.convertToFindRequirementResponseByIdsResultRR() =
    this.let { requirementResponse ->
        FindRequirementResponseByIdsResult.Qualification.RequirementResponse(
            id = requirementResponse.id,
            value = requirementResponse.value,
            relatedTenderer = requirementResponse.relatedTenderer
                .let {
                    FindRequirementResponseByIdsResult.Qualification.RequirementResponse.RelatedTenderer(
                        id = it.id
                    )
                },
            requirement = requirementResponse.requirement
                .let { FindRequirementResponseByIdsResult.Qualification.RequirementResponse.Requirement(id = it.id) },
            responder = requirementResponse.responder
                .let {
                    FindRequirementResponseByIdsResult.Qualification.RequirementResponse.Responder(
                        id = it.id,
                        name = it.name
                    )
                }
        )
    }
