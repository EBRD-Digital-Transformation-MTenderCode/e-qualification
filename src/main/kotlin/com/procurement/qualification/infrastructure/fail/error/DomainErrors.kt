package com.procurement.qualification.infrastructure.fail.error

import com.procurement.qualification.application.service.Logger
import com.procurement.qualification.infrastructure.fail.Fail

sealed class DomainErrors(numberError: String, override val description: String) :
    Fail.Error(prefix = "DE-") {

    override fun logging(logger: Logger) {
        logger.error(message)
    }

    override val code: String = prefix + numberError

    class InvalidScale(className: String, currentScale: Int, availableScale: Int) : DomainErrors(
        numberError = "1",
        description = "Unable to instantiate '${className}' class because passed invalid scale '$currentScale', the available scale is '$availableScale'."
    )

    class IncorrectValue(className: String, value: Any, reason: String? = "") : DomainErrors(
        numberError = "2",
        description = "Unable to instantiate '${className}' class because passed incorrect value '$value'. Reason: '$reason'."
    )
}
