package com.procurement.qualification.infrastructure.handler

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.qualification.infrastructure.web.dto.Action

interface Handler<T : Action, R: Any> {
    val action: T
    fun handle(node: JsonNode): R
}