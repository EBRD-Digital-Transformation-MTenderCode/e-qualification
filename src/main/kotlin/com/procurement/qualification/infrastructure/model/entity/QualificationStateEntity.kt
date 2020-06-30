package com.procurement.qualification.infrastructure.model.entity

import com.procurement.qualification.domain.enums.OperationType
import com.procurement.qualification.domain.enums.Pmd

data class QualificationStateEntity(
    val country: String,
    val pmd: Pmd,
    val operationType: OperationType,
    val parameter: String,
    val value: String
)
