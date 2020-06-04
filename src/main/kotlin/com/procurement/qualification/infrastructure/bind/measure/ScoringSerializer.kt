package com.procurement.qualification.infrastructure.bind.measure

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.procurement.qualification.domain.model.measure.Scoring
import java.io.IOException

class ScoringSerializer : JsonSerializer<Scoring>() {
    companion object {
        fun serialize(scoring: Scoring): String = "%.3f".format(scoring.value)
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(scoring: Scoring, jsonGenerator: JsonGenerator, provider: SerializerProvider) =
        jsonGenerator.writeNumber(serialize(scoring))
}
