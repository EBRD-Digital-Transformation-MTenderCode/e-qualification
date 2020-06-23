package com.procurement.qualification.infrastructure.handler.find.requirementresponsebyids

import com.procurement.qualification.application.model.params.FindRequirementResponseByIdsParams
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.infrastructure.fail.Fail

fun FindRequirementResponseByIdsRequest.convert(): Result<FindRequirementResponseByIdsParams, Fail> =
    FindRequirementResponseByIdsParams.tryCreate(requirementResponseIds, qualificationId, cpid, ocid)
