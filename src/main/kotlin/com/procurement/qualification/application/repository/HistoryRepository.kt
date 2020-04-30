package com.procurement.qualification.application.repository

import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.model.entity.HistoryEntity

interface HistoryRepository {
    fun getHistory(operationId: String, command: String): Result<HistoryEntity?, Fail.Incident>
    fun saveHistory(operationId: String, command: String, result: Any): Result<HistoryEntity, Fail.Incident>
}
