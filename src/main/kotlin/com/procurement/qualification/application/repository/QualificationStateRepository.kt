package com.procurement.qualification.application.repository

import com.procurement.qualification.domain.enums.OperationType
import com.procurement.qualification.domain.enums.Pmd
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.model.entity.QualificationStateEntity

interface QualificationStateRepository {

    fun findBy(country: String, pmd: Pmd, operationType: OperationType): Result<QualificationStateEntity, Fail>
}
