package com.procurement.qualification.domain.util.extension

import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.infrastructure.fail.Fail
import java.util.*

fun String.tryUUID(): Result<UUID, Fail.Incident.Transform.Parsing> =
    try {
        Result.success(UUID.fromString(this))
    } catch (ex: Exception) {
        Result.failure(
            Fail.Incident.Transform.Parsing(UUID::class.java.canonicalName, ex)
        )
    }