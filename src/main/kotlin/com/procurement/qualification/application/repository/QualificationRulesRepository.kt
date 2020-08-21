package com.procurement.qualification.application.repository

import com.procurement.qualification.domain.enums.OperationType
import com.procurement.qualification.domain.enums.ProcurementMethodDetails
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.infrastructure.fail.Fail

interface QualificationRulesRepository {

    fun findBy(
        country: String,
        pmd: ProcurementMethodDetails,
        operationType: OperationType? = null,
        parameter: String
    ): Result<String?, Fail>
}
