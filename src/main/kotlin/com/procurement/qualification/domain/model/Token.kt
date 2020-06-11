package com.procurement.qualification.domain.model

import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.util.extension.tryUUID
import com.procurement.qualification.infrastructure.fail.Fail
import java.util.*

typealias Token = UUID

fun String.tryToken(): Result<Token, Fail.Incident.Transform.Parsing> =
    this.tryUUID()

