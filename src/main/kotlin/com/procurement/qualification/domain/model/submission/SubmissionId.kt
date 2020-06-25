package com.procurement.qualification.domain.model.submission

import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.util.extension.tryUUID
import com.procurement.qualification.infrastructure.fail.error.DataErrors
import java.util.*

typealias SubmissionId = UUID

fun tryCreateSubmissionId(value: String): Result<SubmissionId, DataErrors.Validation.DataFormatMismatch> = value.tryUUID()
    .doReturn {
        return Result.failure(
            DataErrors.Validation.DataFormatMismatch(
                name = "id",
                actualValue = value,
                expectedFormat = "uuid"
            )
        )
    }
    .asSuccess()
