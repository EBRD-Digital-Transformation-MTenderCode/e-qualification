package com.procurement.qualification.infrastructure.model.entity

import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid

data class QualificationEntity(
    val cpid: Cpid,
    val ocid: Ocid,
    val jsonData: String
)
