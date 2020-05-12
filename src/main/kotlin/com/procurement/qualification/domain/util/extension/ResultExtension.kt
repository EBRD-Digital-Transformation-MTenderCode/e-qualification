package com.procurement.qualification.domain.util.extension

import com.procurement.qualification.domain.functional.Result

fun <E> Result<String, E>.transformToString(): String {
    return when (this) {
        is Result.Success -> this.get
        else -> this.error.toString()
    }
}