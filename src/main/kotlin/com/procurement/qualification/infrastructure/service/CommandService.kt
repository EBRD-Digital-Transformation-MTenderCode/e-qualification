package com.procurement.qualification.infrastructure.service

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.qualification.application.service.Logger
import com.procurement.qualification.infrastructure.fail.error.BadRequest
import com.procurement.qualification.infrastructure.handler.previous.generation.historical.SavePeriodHandler
import com.procurement.qualification.infrastructure.handler.previous.generation.validation.ValidatePeriodHandler
import com.procurement.qualification.infrastructure.web.dto.command.CommandType
import com.procurement.qualification.infrastructure.web.dto.response.ApiResponse
import com.procurement.qualification.infrastructure.web.parser.tryGetCommand
import com.procurement.qualification.infrastructure.web.response.generator.ApiResponseGenerator.generateResponseOnFailure
import org.springframework.stereotype.Service

@Service
class CommandService(
    private val validatePeriodHandler: ValidatePeriodHandler,
    private val savePeriodHandler: SavePeriodHandler,
    private val logger: Logger
) {

    fun execute(node: JsonNode): ApiResponse {
        val command = node.tryGetCommand()
            .doReturn {
                return generateResponseOnFailure(
                    fail = BadRequest(), logger = logger
                )
            }
        return when (command) {
            CommandType.VALIDATE_PERIOD -> validatePeriodHandler.handle(node)
            CommandType.SAVE_PERIOD -> savePeriodHandler.handle(node)
        }
    }
}
