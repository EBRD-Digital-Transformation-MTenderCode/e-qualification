package com.procurement.qualification.domain.model

import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.util.extension.tryUUID
import com.procurement.qualification.infrastructure.fail.Fail
import java.util.*

typealias Owner = UUID

fun String.tryOwner(): Result<Owner, Fail.Incident.Transform.Parsing> =
    this.tryUUID()