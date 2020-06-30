package com.procurement.qualification.infrastructure.model.entity

import com.procurement.qualification.domain.enums.OperationType
import com.procurement.qualification.domain.enums.Pmd

data class QualificationRulesEntity(
    val country: String,
    val pmd: Pmd,
    val operationType: OperationType,
    val parameter: String,
    val value: String
)
