package com.procurement.qualification.domain.model.date

import com.procurement.qualification.domain.functional.Result
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

private const val formatPattern = "uuuu-MM-dd'T'HH:mm:ss'Z'"
private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(formatPattern)
    .withResolverStyle(ResolverStyle.STRICT)

fun LocalDateTime.format(): String = this.format(formatter)

fun String.parse(): LocalDateTime = LocalDateTime.parse(this, formatter)

fun String.tryParseToLocalDateTime(): Result<LocalDateTime, String> = try {
    Result.success(LocalDateTime.parse(this, formatter))
} catch (ignore: Exception) {
    Result.failure(formatPattern)
}