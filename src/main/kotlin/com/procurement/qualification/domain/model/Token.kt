package com.procurement.qualification.domain.model

import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.util.extension.tryUUID
import com.procurement.qualification.infrastructure.fail.error.DataErrors
import java.util.*

typealias Token = UUID

fun String.tryToken(): Result<Token, DataErrors.Validation.DataFormatMismatch> =
    when (val result = this.tryUUID()) {
        is Result.Success -> result
        is Result.Failure -> Result.failure(
            DataErrors.Validation.DataFormatMismatch(
                name = "token",
                actualValue = this,
                expectedFormat = "uuid"
            )
        )
    }

