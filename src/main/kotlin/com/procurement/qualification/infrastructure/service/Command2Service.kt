package com.procurement.qualification.infrastructure.service

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.qualification.application.service.Logger
import com.procurement.qualification.infrastructure.handler.validation.CheckPeriod2Handler
import com.procurement.qualification.infrastructure.web.dto.response.ApiResponse2
import com.procurement.qualification.infrastructure.web.enums.Command2Type
import com.procurement.qualification.infrastructure.web.parser.tryGetAction
import com.procurement.qualification.infrastructure.web.parser.tryGetId
import com.procurement.qualification.infrastructure.web.parser.tryGetVersion
import com.procurement.qualification.infrastructure.web.response.generator.ApiResponse2Generator.generateResponseOnFailure
import org.springframework.stereotype.Service

@Service
class Command2Service(
    private val checkPeriod2Handler: CheckPeriod2Handler,
    private val logger: Logger
) {

    fun execute(node: JsonNode): ApiResponse2 {
        val action = node.tryGetAction()
            .doReturn { error ->
                return generateResponseOnFailure(
                    fail = error, id = node.tryGetId().get, version = node.tryGetVersion().get, logger = logger
                )
            }
        return when (action) {
            Command2Type.CHECK_PERIOD_2 -> checkPeriod2Handler.handle(node)
        }
    }
}