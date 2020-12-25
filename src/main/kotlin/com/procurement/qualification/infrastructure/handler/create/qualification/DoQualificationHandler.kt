package com.procurement.qualification.infrastructure.handler.create.qualification

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.qualification.application.service.Logger
import com.procurement.qualification.application.service.QualificationService
import com.procurement.qualification.application.service.Transform
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asFailure
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.functional.bind
import com.procurement.qualification.domain.util.extension.errorIfBlank
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.fail.error.DataErrors
import com.procurement.qualification.infrastructure.handler.exception.EmptyStringException
import com.procurement.qualification.infrastructure.handler.general.AbstractHandler2
import com.procurement.qualification.infrastructure.web.enums.Command2Type
import com.procurement.qualification.infrastructure.web.parser.tryGetParams
import org.springframework.stereotype.Component

@Component
class DoQualificationHandler(
    transform: Transform,
    logger: Logger,
    val qualificationService: QualificationService
) : AbstractHandler2<Command2Type, DoQualificationResult>(
    transform = transform,
    logger = logger
) {
    override fun execute(node: JsonNode): Result<DoQualificationResult, Fail> {

        val params = node.tryGetParams(target = DoQualificationRequest::class.java, transform = transform)
            .bind { it.validateTextAttributes() }
            .bind { it.convert() }
            .orForwardFail { fail -> return fail }

        return qualificationService.doQualification(params = params)
    }

    override val action: Command2Type = Command2Type.DO_QUALIFICATION

    private fun DoQualificationRequest.validateTextAttributes(): Result<DoQualificationRequest, DataErrors.Validation.EmptyString> {
        try {
            qualifications.forEachIndexed { i, qualification ->
                qualification.internalId.checkForBlank("qualifications[$i].internalId")
                qualification.description.checkForBlank("qualifications[$i].description")
                qualification.documents?.forEachIndexed { j, document ->
                    document.title.checkForBlank("qualifications[$i].documents[$j].title")
                    document.description.checkForBlank("qualifications[$i].documents[$j].description")
                }
            }
        } catch (exception: EmptyStringException) {
            return DataErrors.Validation.EmptyString(exception.attributeName).asFailure()
        }

        return this.asSuccess()
    }

    private fun String?.checkForBlank(name: String) = this.errorIfBlank { EmptyStringException(name) }
}
