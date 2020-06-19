package com.procurement.qualification.domain.model

import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.util.extension.tryUUID
import com.procurement.qualification.infrastructure.fail.error.DataErrors
import java.util.*

typealias Owner = UUID

fun String.tryOwner(): Result<Owner, DataErrors.Validation.DataFormatMismatch> =
    when (val result = this.tryUUID()) {
        is Result.Success -> result
        is Result.Failure -> Result.failure(
            DataErrors.Validation.DataFormatMismatch(
                name = "owner",
                actualValue = this,
                expectedFormat = "uuid"
            )
        )
    }