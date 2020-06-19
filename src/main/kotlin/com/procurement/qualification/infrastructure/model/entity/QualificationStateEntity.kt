package com.procurement.qualification.infrastructure.model.entity

import com.procurement.qualification.domain.enums.OperationType
import com.procurement.qualification.domain.enums.Pmd
import com.procurement.qualification.domain.enums.QualificationStatus
import com.procurement.qualification.domain.enums.QualificationStatusDetails

data class QualificationStateEntity(
    val country: String,
    val pmd: Pmd,
    val operationType: OperationType,
    val status: QualificationStatus,
    val statusDetails: QualificationStatusDetails
)
