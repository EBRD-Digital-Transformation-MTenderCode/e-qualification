package com.procurement.qualification.infrastructure.handler.validation

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.qualification.application.service.Logger
import com.procurement.qualification.domain.functional.ValidationResult
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.handler.Handler
import com.procurement.qualification.infrastructure.web.dto.Action
import com.procurement.qualification.infrastructure.web.dto.response.ApiResponse2
import com.procurement.qualification.infrastructure.web.dto.response.ApiSuccessResponse2
import com.procurement.qualification.infrastructure.web.parser.tryGetId
import com.procurement.qualification.infrastructure.web.parser.tryGetVersion
import com.procurement.qualification.infrastructure.web.response.generator.ApiResponse2Generator

abstract class AbstractValidationHandler2<ACTION : Action, E : Fail>(
    private val logger: Logger, private val apiResponse2Generator: ApiResponse2Generator
) : Handler<ACTION, ApiResponse2> {

    override fun handle(node: JsonNode): ApiResponse2 {
        val id = node.tryGetId().get
        val version = node.tryGetVersion().get

        return when (val result = execute(node)) {
            is ValidationResult.Ok -> {
                if (logger.isDebugEnabled)
                    logger.debug("${action.key} has been executed.")
                ApiSuccessResponse2(version = version, id = id)
            }
            is ValidationResult.Fail -> apiResponse2Generator.generateResponseOnFailure(
                fail = result.error, version = version, id = id, logger = logger
            )
        }
    }

    abstract fun execute(node: JsonNode): ValidationResult<E>
}
