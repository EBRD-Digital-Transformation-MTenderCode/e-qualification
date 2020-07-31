package com.procurement.qualification.infrastructure.handler.finalize

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.qualification.application.repository.HistoryRepository
import com.procurement.qualification.application.service.Logger
import com.procurement.qualification.application.service.QualificationService
import com.procurement.qualification.application.service.Transform
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.handler.historical.AbstractHistoricalHandler2
import com.procurement.qualification.infrastructure.web.enums.Command2Type
import com.procurement.qualification.infrastructure.web.parser.tryGetParams
import org.springframework.stereotype.Component

@Component
class FinalizeQualificationsHandler(
    historyRepository: HistoryRepository,
    transform: Transform,
    logger: Logger,
    private val qualificationService: QualificationService
) : AbstractHistoricalHandler2<Command2Type, FinalizeQualificationsResult>(
    target = FinalizeQualificationsResult::class.java,
    historyRepository = historyRepository,
    transform = transform,
    logger = logger
) {
    override fun execute(node: JsonNode): Result<FinalizeQualificationsResult, Fail> {

        val params = node.tryGetParams(target = FinalizeQualificationsRequest::class.java, transform = transform)
            .orForwardFail { fail -> return fail }
            .convert()
            .orForwardFail { fail -> return fail }

        return qualificationService.finalizeQualifications(params = params)
    }

    override val action: Command2Type = Command2Type.FINALIZE_QUALIFICATIONS
}
