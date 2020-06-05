package com.procurement.qualification.infrastructure.bind.requirement.value

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.procurement.qualification.domain.exception.RequirementValueException
import com.procurement.qualification.domain.model.requirement.RequirementResponseValue
import java.io.IOException
import java.math.BigDecimal

class RequirementValueDeserializer : JsonDeserializer<RequirementResponseValue>() {
    companion object {
        fun deserialize(value: String): RequirementResponseValue = RequirementResponseValue.AsString(value)
        fun deserialize(value: Boolean): RequirementResponseValue = RequirementResponseValue.AsBoolean(value)
        fun deserialize(value: BigDecimal): RequirementResponseValue = RequirementResponseValue.AsNumber(value)
        fun deserialize(value: Long): RequirementResponseValue = RequirementResponseValue.AsInteger(value)
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(
        jsonParser: JsonParser,
        deserializationContext: DeserializationContext
    ): RequirementResponseValue = when (jsonParser.currentToken) {
        JsonToken.VALUE_STRING -> deserialize(
            jsonParser.text
        )
        JsonToken.VALUE_FALSE -> deserialize(
            false
        )
        JsonToken.VALUE_TRUE -> deserialize(
            true
        )
        JsonToken.VALUE_NUMBER_INT -> deserialize(
            jsonParser.longValue
        )
        JsonToken.VALUE_NUMBER_FLOAT -> deserialize(
            BigDecimal(jsonParser.text)
        )
        else -> throw RequirementValueException(
            requirementValue = jsonParser.text,
            description = "Invalid type"
        )
    }
}
