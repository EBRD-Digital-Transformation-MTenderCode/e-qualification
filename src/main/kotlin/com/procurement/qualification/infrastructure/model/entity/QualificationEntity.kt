package com.procurement.qualification.infrastructure.model.entity

import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.domain.model.qualification.QualificationId

data class QualificationEntity(
    val cpid: Cpid,
    val ocid: Ocid,
    val id: QualificationId,
    val jsonData: String
)
