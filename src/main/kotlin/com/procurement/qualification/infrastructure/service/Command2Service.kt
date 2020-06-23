package com.procurement.qualification.infrastructure.service

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.qualification.application.service.Logger
import com.procurement.qualification.infrastructure.handler.create.qualifications.CreateQualificationsHandler
import com.procurement.qualification.infrastructure.handler.determine.nextforqualification.DetermineNextsForQualificationHandler
import com.procurement.qualification.infrastructure.handler.find.qualificationids.FindQualificationIdsHandler
import com.procurement.qualification.infrastructure.web.dto.response.ApiResponse2
import com.procurement.qualification.infrastructure.web.enums.Command2Type
import com.procurement.qualification.infrastructure.web.parser.tryGetAction
import com.procurement.qualification.infrastructure.web.parser.tryGetId
import com.procurement.qualification.infrastructure.web.parser.tryGetVersion
import com.procurement.qualification.infrastructure.web.response.generator.ApiResponse2Generator.generateResponseOnFailure
import org.springframework.stereotype.Service

@Service
class Command2Service(
    private val logger: Logger,
    private val findQualificationIdsHandler: FindQualificationIdsHandler,
    private val createQualificationsHandler: CreateQualificationsHandler,
    private val determineNextsForQualificationHandler: DetermineNextsForQualificationHandler
) {

    fun execute(node: JsonNode): ApiResponse2 {

        val version = node.tryGetVersion()
            .doReturn { versionFail ->
                val id = node.tryGetId()
                    .doReturn { idFail -> return generateResponseOnFailure(fail = idFail, logger = logger) }
                return generateResponseOnFailure(fail = versionFail, logger = logger, id = id)
            }

        val id = node.tryGetId()
            .doReturn { fail ->
                return generateResponseOnFailure(fail = fail, version = version, logger = logger)
            }

        val action = node.tryGetAction()
            .doReturn { error ->
                return generateResponseOnFailure(fail = error, id = id, version = version, logger = logger)
            }

        return when (action) {
            Command2Type.FIND_QUALIFICATION_IDS -> findQualificationIdsHandler.handle(node = node)
            Command2Type.CREATE_QUALIFICATIONS -> createQualificationsHandler.handle(node = node)
            Command2Type.DETERMINE_NEXTS_FOR_QUALIFICATION -> determineNextsForQualificationHandler.handle(node = node)
        }
    }
}