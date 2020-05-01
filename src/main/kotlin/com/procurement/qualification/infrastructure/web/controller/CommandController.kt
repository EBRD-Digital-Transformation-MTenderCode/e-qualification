package com.procurement.qualification.infrastructure.web.controller

import com.procurement.qualification.application.service.Logger
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.service.CommandService
import com.procurement.qualification.infrastructure.utils.toJson
import com.procurement.qualification.infrastructure.web.dto.response.ApiResponse
import com.procurement.qualification.infrastructure.web.parser.tryGetNode
import com.procurement.qualification.infrastructure.web.response.generator.ApiResponseGenerator.generateResponseOnFailure
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/command")
class CommandController(
    private val commandService: CommandService,
    private val logger: Logger
) {

    @PostMapping
    fun command(@RequestBody requestBody: String): ResponseEntity<ApiResponse> {
        if (logger.isDebugEnabled)
            logger.debug("RECEIVED COMMAND: '$requestBody'.")

        val node = requestBody.tryGetNode()
            .doReturn { error -> return generateResponseEntityOnFailure(fail = error) }

        val response =
            commandService.execute(node)
                .also { response ->
                    if (logger.isDebugEnabled)
                        logger.debug("RESPONSE (id: '${response.id}'): '${response.toJson()}'.")
                }

        return ResponseEntity(response, HttpStatus.OK)
    }

    private fun generateResponseEntityOnFailure(fail: Fail): ResponseEntity<ApiResponse> {
        val response = generateResponseOnFailure(fail = fail, logger = logger)
        return ResponseEntity(response, HttpStatus.OK)
    }
}
