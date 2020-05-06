package com.procurement.qualification.infrastructure.converter

import com.procurement.qualification.application.model.period.check.params.CheckPeriod2Params
import com.procurement.qualification.infrastructure.web.dto.request.period.CheckPeriod2Request

fun CheckPeriod2Request.convert() = CheckPeriod2Params.tryCreate(cpid = cpid, ocid = ocid, date = date)
