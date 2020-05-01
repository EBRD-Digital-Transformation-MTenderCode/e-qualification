package com.procurement.qualification.infrastructure.web.controller

import com.procurement.qualification.application.service.Logger
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.infrastructure.configuration.properties.GlobalProperties2
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.service.Command2Service
import com.procurement.qualification.infrastructure.utils.toJson
import com.procurement.qualification.infrastructure.web.dto.ApiVersion2
import com.procurement.qualification.infrastructure.web.dto.response.ApiResponse2
import com.procurement.qualification.infrastructure.web.parser.NaN
import com.procurement.qualification.infrastructure.web.parser.tryGetId
import com.procurement.qualification.infrastructure.web.parser.tryGetNode
import com.procurement.qualification.infrastructure.web.parser.tryGetVersion
import com.procurement.qualification.infrastructure.web.response.generator.ApiResponse2Generator.generateResponseOnFailure
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/command2")
class Command2Controller(
    private val command2Service: Command2Service,
    private val logger: Logger
) {

    @PostMapping
    fun command(@RequestBody requestBody: String): ResponseEntity<ApiResponse2> {
        if (logger.isDebugEnabled)
            logger.debug("RECEIVED COMMAND: '$requestBody'.")

        val node = requestBody.tryGetNode()
            .doReturn { error -> return generateResponseEntityOnFailure(fail = error) }

        val version = when (val versionResult = node.tryGetVersion()) {
            is Result.Success -> versionResult.get
            is Result.Failure -> {
                return when (val idResult = node.tryGetId()) {
                    is Result.Success -> generateResponseEntityOnFailure(
                        fail = versionResult.error,
                        id = idResult.get
                    )
                    is Result.Failure -> generateResponseEntityOnFailure(fail = versionResult.error)
                }
            }
        }

        val id = node.tryGetId()
            .doReturn { error -> return generateResponseEntityOnFailure(fail = error, version = version) }

        val response =
            command2Service.execute(node)
                .also { response ->
                    if (logger.isDebugEnabled)
                        logger.debug("RESPONSE (id: '${id}'): '${response.toJson()}'.")
                }

        return ResponseEntity(response, HttpStatus.OK)
    }

    private fun generateResponseEntityOnFailure(
        fail: Fail, version: ApiVersion2 = GlobalProperties2.App.apiVersion, id: UUID = NaN
    ): ResponseEntity<ApiResponse2> {
        val response = generateResponseOnFailure(
            fail = fail, id = id, version = version, logger = logger
        )
        return ResponseEntity(response, HttpStatus.OK)
    }
}