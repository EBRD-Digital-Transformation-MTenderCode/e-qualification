package com.procurement.qualification.application.repository

import com.procurement.qualification.domain.enums.ProcurementMethod
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.infrastructure.fail.Fail

interface PeriodRulesRepository {
    fun findTermBy(country: String, pmd: ProcurementMethod): Result<Long?, Fail.Incident>
}
