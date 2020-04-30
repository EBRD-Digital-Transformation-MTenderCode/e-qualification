package com.procurement.qualification.infrastructure.service

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.qualification.application.repository.HistoryRepository
import com.procurement.qualification.application.service.Logger
import com.procurement.qualification.infrastructure.web.dto.response.ApiResponse
import org.springframework.stereotype.Service

@Service
class CommandService(
    private val historyRepository: HistoryRepository,
    private val logger: Logger
) {

    fun execute(node: JsonNode): ApiResponse {
        return TODO()
    }
}
