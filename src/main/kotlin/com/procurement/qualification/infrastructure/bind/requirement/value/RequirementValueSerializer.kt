package com.procurement.qualification.infrastructure.bind.requirement.value

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.procurement.qualification.domain.model.requirement.RequirementResponseValue
import java.io.IOException

class RequirementValueSerializer : JsonSerializer<RequirementResponseValue>() {
    companion object {
        fun serialize(requirementValue: RequirementResponseValue.AsString): String = requirementValue.value
        fun serialize(requirementValue: RequirementResponseValue.AsBoolean): Boolean = requirementValue.value
        fun serialize(requirementValue: RequirementResponseValue.AsNumber): String = "%.3f".format(requirementValue.value)
        fun serialize(requirementValue: RequirementResponseValue.AsInteger): Long = requirementValue.value
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(
        requirementValue: RequirementResponseValue,
        jsonGenerator: JsonGenerator,
        provider: SerializerProvider
    ) {
        when (requirementValue) {
            is RequirementResponseValue.AsString -> jsonGenerator.writeString(
                serialize(
                    requirementValue
                )
            )
            is RequirementResponseValue.AsNumber -> jsonGenerator.writeNumber(
                serialize(
                    requirementValue
                )
            )
            is RequirementResponseValue.AsBoolean -> jsonGenerator.writeBoolean(
                serialize(
                    requirementValue
                )
            )
            is RequirementResponseValue.AsInteger -> jsonGenerator.writeNumber(
                serialize(
                    requirementValue
                )
            )
        }
    }
}
