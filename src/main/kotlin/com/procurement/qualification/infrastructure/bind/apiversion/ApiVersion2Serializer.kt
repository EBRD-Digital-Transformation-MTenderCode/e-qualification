package com.procurement.qualification.infrastructure.bind.apiversion

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.procurement.qualification.infrastructure.web.dto.ApiVersion2
import java.io.IOException

class ApiVersion2Serializer : JsonSerializer<ApiVersion2>() {
    companion object {
        fun serialize(apiVersion: ApiVersion2): String = "${apiVersion.major}.${apiVersion.minor}.${apiVersion.patch}"
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(apiVersion: ApiVersion2, jsonGenerator: JsonGenerator, provider: SerializerProvider) {
        jsonGenerator.writeString(serialize(apiVersion))
    }
}
