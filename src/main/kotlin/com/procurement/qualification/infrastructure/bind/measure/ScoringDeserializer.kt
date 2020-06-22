package com.procurement.qualification.infrastructure.bind.measure

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.procurement.qualification.domain.model.measure.Scoring
import java.io.IOException

class ScoringDeserializer : JsonDeserializer<Scoring>() {
    companion object {
        fun deserialize(text: String): Scoring = Scoring.tryCreate(text)
            .orThrow { error -> IllegalArgumentException("Incorrect value of the scoring: '$text'. ${error.description}") }
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): Scoring {
        if (jsonParser.currentToken != JsonToken.VALUE_NUMBER_FLOAT
            && jsonParser.currentToken != JsonToken.VALUE_NUMBER_INT
        ) {
            throw IllegalArgumentException("Incorrect value of the scoring: '${jsonParser.text}'. The value must be a real number.")
        }
        return deserialize(jsonParser.text)
    }
}
