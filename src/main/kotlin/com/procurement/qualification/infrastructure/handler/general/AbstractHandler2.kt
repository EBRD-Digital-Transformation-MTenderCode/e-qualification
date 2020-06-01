package com.procurement.qualification.infrastructure.handler.general

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.qualification.application.service.Logger
import com.procurement.qualification.application.service.Transform
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.handler.Handler
import com.procurement.qualification.infrastructure.web.dto.Action
import com.procurement.qualification.infrastructure.web.dto.response.ApiResponse2
import com.procurement.qualification.infrastructure.web.dto.response.ApiSuccessResponse2
import com.procurement.qualification.infrastructure.web.parser.tryGetId
import com.procurement.qualification.infrastructure.web.parser.tryGetVersion
import com.procurement.qualification.infrastructure.web.response.generator.ApiResponse2Generator.generateResponseOnFailure

abstract class AbstractHandler2<ACTION : Action, R : Any>(
    val transform: Transform,
    private val logger: Logger
) : Handler<ACTION, ApiResponse2> {

    override fun handle(node: JsonNode): ApiResponse2 {
        val id = node.tryGetId().get
        val version = node.tryGetVersion().get

        return when (val result = execute(node)) {
            is Result.Success -> {
                if (logger.isDebugEnabled)
                    logger.debug("${action.key} has been executed. Result: '${transform.trySerialization(result.get)}'")
                return ApiSuccessResponse2(
                    version = version,
                    id = id,
                    result = result.get
                )
            }
            is Result.Failure -> generateResponseOnFailure(
                fail = result.error,
                version = version,
                id = id,
                logger = logger
            )
        }
    }

    abstract fun execute(node: JsonNode): Result<R, Fail>
}