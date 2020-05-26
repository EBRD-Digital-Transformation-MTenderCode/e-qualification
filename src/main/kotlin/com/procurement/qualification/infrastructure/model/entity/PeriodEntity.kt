package com.procurement.qualification.infrastructure.model.entity

import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import java.time.LocalDateTime

data class PeriodEntity(
    val cpid: Cpid,
    val ocid: Ocid,
    val startDate: LocalDateTime,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    val endDate: LocalDateTime? = null
)