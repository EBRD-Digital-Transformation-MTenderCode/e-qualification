package com.procurement.qualification.domain.util.extension

import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.Result.Companion.failure
import com.procurement.qualification.domain.functional.Result.Companion.success
import com.procurement.qualification.infrastructure.fail.error.DataErrors

fun <T> List<T>.getElementIfOnlyOne(name: String): Result<T, DataErrors> {
    if (this.isEmpty())
        return failure(DataErrors.Validation.EmptyArray(name = name))

    return if (this.size != 1)
        failure(
            DataErrors.Validation.InvalidNumberOfElementsInArray(
                name = name,
                actualLength = this.size,
                max = 1,
                min = 1
            )
        )
    else
        success(this[0])
}
