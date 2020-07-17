package com.procurement.qualification.infrastructure.handler.analyze.qualification

import com.procurement.qualification.application.model.params.AnalyzeQualificationsForInvitationParams
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.infrastructure.fail.error.DataErrors

fun AnalyzeQualificationsForInvitationRequest.convert(): Result<AnalyzeQualificationsForInvitationParams, DataErrors> =
    AnalyzeQualificationsForInvitationParams.tryCreate(
        cpid = cpid, ocid = ocid, country = country, pmd = pmd
    )
