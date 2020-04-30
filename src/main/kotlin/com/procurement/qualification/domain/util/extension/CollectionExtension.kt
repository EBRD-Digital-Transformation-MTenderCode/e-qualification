package com.procurement.qualification.domain.util.extension

import com.procurement.qualification.domain.functional.Option
import com.procurement.qualification.domain.functional.Result

fun <T, R, E> List<T>?.mapOptionalResult(block: (T) -> Result<R, E>): Result<Option<List<R>>, E> {
    if (this == null)
        return Result.success(Option.none())

    val r = mutableListOf<R>()
    for (element in this) {
        when (val result = block(element)) {
            is Result.Success -> r.add(result.get)
            is Result.Failure -> return result
        }
    }
    return Result.success(Option.pure(r))
}

fun <T, R, E> List<T>.mapResult(block: (T) -> Result<R, E>): Result<List<R>, E> {
    val r = mutableListOf<R>()
    for (element in this) {
        when (val result = block(element)) {
            is Result.Success -> r.add(result.get)
            is Result.Failure -> return result
        }
    }
    return Result.success(r)
}

fun <T> T?.toListOrEmpty(): List<T> = if (this != null) listOf(this) else emptyList()
