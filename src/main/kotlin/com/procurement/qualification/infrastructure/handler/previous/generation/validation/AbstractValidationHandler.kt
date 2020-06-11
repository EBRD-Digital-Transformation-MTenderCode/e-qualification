package com.procurement.qualification.infrastructure.handler.previous.generation.validation

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.qualification.application.service.Logger
import com.procurement.qualification.application.service.Transform
import com.procurement.qualification.domain.functional.ValidationResult
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.fail.error.BadRequest
import com.procurement.qualification.infrastructure.handler.Handler
import com.procurement.qualification.infrastructure.web.dto.command.CommandMessage
import com.procurement.qualification.infrastructure.web.dto.command.CommandType
import com.procurement.qualification.infrastructure.web.dto.response.ApiResponse
import com.procurement.qualification.infrastructure.web.dto.response.ApiSuccessResponse
import com.procurement.qualification.infrastructure.web.response.generator.ApiResponseGenerator.generateResponseOnFailure

abstract class AbstractValidationHandler<ACTION : CommandType, E : Fail>(
    private val transform: Transform,
    private val logger: Logger
) : Handler<ACTION, ApiResponse> {

    override fun handle(node: JsonNode): ApiResponse {
        val cm = transform.tryMapping(node, CommandMessage::class.java)
            .doReturn { return generateResponseOnFailure(fail = BadRequest(), logger = logger) }

        return when (val result = execute(cm)) {
            is ValidationResult.Ok -> {
                if (logger.isDebugEnabled)
                    logger.debug("${action.key} has been executed.")
                ApiSuccessResponse(
                    id = cm.id, version = cm.version, data = Unit
                )
            }
            is ValidationResult.Fail -> generateResponseOnFailure(
                fail = result.error, id = cm.id, version = cm.version, logger = logger
            )
        }
    }

    abstract fun execute(cm: CommandMessage): ValidationResult<E>
}
