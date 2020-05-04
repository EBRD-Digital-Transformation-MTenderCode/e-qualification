package com.procurement.qualification.domain.util.extension

import com.procurement.qualification.domain.functional.Result
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

private const val FORMAT_PATTERN = "uuuu-MM-dd'T'HH:mm:ss'Z'"
private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(FORMAT_PATTERN)
    .withResolverStyle(ResolverStyle.STRICT)

fun LocalDateTime.format(): String = this.format(formatter)

fun String.parseLocalDateTime(): LocalDateTime = LocalDateTime.parse(this, formatter)

fun nowDefaultUTC(): LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)

fun String.tryParseLocalDateTime(): Result<LocalDateTime, String> = try {
    Result.success(this.parseLocalDateTime())
} catch (ignore: Exception) {
    Result.failure(FORMAT_PATTERN)
}