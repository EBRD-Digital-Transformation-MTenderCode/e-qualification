package com.procurement.qualification.infrastructure.bind.databinding

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.procurement.qualification.domain.model.date.parse
import java.time.LocalDateTime

class JsonDateTimeDeserializer : JsonDeserializer<LocalDateTime>() {
    companion object {
        fun deserialize(value: String): LocalDateTime = value.parse()
    }

    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): LocalDateTime =
        deserialize(jsonParser.text)
}
