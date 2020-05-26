package com.procurement.qualification.infrastructure.handler.previous.generation.historical

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.qualification.application.repository.HistoryRepository
import com.procurement.qualification.application.service.Logger
import com.procurement.qualification.application.service.Transform
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.util.extension.transformToString
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.fail.error.BadRequest
import com.procurement.qualification.infrastructure.handler.Handler
import com.procurement.qualification.infrastructure.repository.HistoryRepositoryCassandra
import com.procurement.qualification.infrastructure.web.dto.command.CommandMessage
import com.procurement.qualification.infrastructure.web.dto.command.CommandType
import com.procurement.qualification.infrastructure.web.dto.response.ApiResponse
import com.procurement.qualification.infrastructure.web.dto.response.ApiSuccessResponse
import com.procurement.qualification.infrastructure.web.response.generator.ApiResponseGenerator.generateResponseOnFailure

abstract class AbstractHistoricalHandler<ACTION : CommandType, R : Any>(
    private val target: Class<R>,
    private val historyRepository: HistoryRepository,
    private val transform: Transform,
    private val logger: Logger
) : Handler<ACTION, ApiResponse> {

    override fun handle(node: JsonNode): ApiResponse {
        val cm = transform.tryMapping(node, CommandMessage::class.java)
            .doReturn { return generateResponseOnFailure(fail = BadRequest(), logger = logger) }

        val history = historyRepository.getHistory(cm.id, action.key)
            .doReturn { incident ->
                return generateResponseOnFailure(fail = incident, id = cm.id, version = cm.version, logger = logger)
            }

        if (history != null) {
            val jsonData = history.jsonData
            val data = transform.tryDeserialization(value = jsonData, target = target)
                .doReturn { incident ->
                    return generateResponseOnFailure(
                        fail = Fail.Incident.Database.Parsing(
                            column = HistoryRepositoryCassandra.JSON_DATA, value = jsonData, exception = incident.exception
                        ),
                        id = cm.id,
                        version = cm.version,
                        logger = logger
                    )
                }
            return ApiSuccessResponse(version = cm.version, id = cm.id, data = data)
        }

        return when (val result = execute(cm)) {
            is Result.Success -> {
                val data = result.get
                historyRepository.saveHistory(cm.id, action.key, data)
                if (logger.isDebugEnabled)
                    logger.debug(
                        "${action.key} has been executed. Result: '${transform.trySerialization(result.get)
                            .transformToString()}'"
                    )

                ApiSuccessResponse(id = cm.id, version = cm.version, data = data)
            }
            is Result.Failure -> generateResponseOnFailure(
                fail = result.error, id = cm.id, version = cm.version, logger = logger
            )
        }
    }

    abstract fun execute(cm: CommandMessage): Result<R, Fail>
}

