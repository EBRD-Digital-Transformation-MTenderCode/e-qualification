package com.procurement.qualification.infrastructure.bind.jackson

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.procurement.qualification.domain.model.measure.Scoring
import com.procurement.qualification.domain.model.qualification.RequirementResponseValue
import com.procurement.qualification.infrastructure.bind.apiversion.ApiVersion2Deserializer
import com.procurement.qualification.infrastructure.bind.apiversion.ApiVersion2Serializer
import com.procurement.qualification.infrastructure.bind.databinding.JsonDateTimeDeserializer
import com.procurement.qualification.infrastructure.bind.databinding.JsonDateTimeSerializer
import com.procurement.qualification.infrastructure.bind.measure.ScoringDeserializer
import com.procurement.qualification.infrastructure.bind.measure.ScoringSerializer
import com.procurement.qualification.infrastructure.bind.requirement.RequirementValueDeserializer
import com.procurement.qualification.infrastructure.bind.requirement.RequirementValueSerializer
import com.procurement.qualification.infrastructure.web.dto.ApiVersion2
import java.time.LocalDateTime

fun ObjectMapper.configuration() {
    val module = SimpleModule().apply {
        /**
         * Serializer/Deserializer for LocalDateTime type
         */
        addSerializer(LocalDateTime::class.java, JsonDateTimeSerializer())
        addDeserializer(LocalDateTime::class.java, JsonDateTimeDeserializer())

        /**
         * Serializer/Deserializer for ApiVersion type
         */
        addSerializer(ApiVersion2::class.java, ApiVersion2Serializer())
        addDeserializer(ApiVersion2::class.java, ApiVersion2Deserializer())

        /**
         * Serializer/Deserializer for measure Scoring
         */
        addSerializer(Scoring::class.java, ScoringSerializer())
        addDeserializer(Scoring::class.java, ScoringDeserializer())

        /**
         * Serializer/Deserializer for RequirementResponseValue
         */
        addSerializer(RequirementResponseValue::class.java, RequirementValueSerializer())
        addDeserializer(RequirementResponseValue::class.java, RequirementValueDeserializer())
    }

    this.registerModule(module)
    this.registerModule(KotlinModule())
    this.configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, true)
    this.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true)
    this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    this.nodeFactory = JsonNodeFactory.withExactBigDecimals(true)
}
