package com.procurement.qualification.infrastructure.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode
import com.procurement.qualification.application.service.Transform
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.Result.Companion.failure
import com.procurement.qualification.domain.functional.Result.Companion.success
import com.procurement.qualification.infrastructure.fail.Fail
import java.io.IOException

class JacksonJsonTransform(private val mapper: ObjectMapper) :
    Transform {

    /**
     * Parsing
     */
    override fun tryParse(value: String): Result<JsonNode, Fail.Incident.Transform.Parsing> = try {
        success(mapper.readTree(value))
    } catch (expected: IOException) {
        failure(Fail.Incident.Transform.Parsing(className = JsonNode::class.java.canonicalName, exception = expected))
    }

    /**
     * Mapping
     */
    override fun <R> tryMapping(value: JsonNode, target: Class<R>): Result<R, Fail.Incident.Transform.Mapping> =
        try {
            if (value is NullNode)
                failure(Fail.Incident.Transform.Mapping(description = "Object to map must not be null."))
            else success(mapper.treeToValue(value, target))
        } catch (expected: Exception) {
            failure(Fail.Incident.Transform.Mapping(description = "Error of mapping.", exception = expected))
        }

    override fun <R> tryMapping(
        value: JsonNode,
        typeRef: TypeReference<R>
    ): Result<R, Fail.Incident.Transform.Mapping> = try {
        val parser = mapper.treeAsTokens(value)
        success(mapper.readValue(parser, typeRef))
    } catch (expected: Exception) {
        failure(Fail.Incident.Transform.Mapping(description = "Error of mapping.", exception = expected))
    }

    /**
     * Deserialization
     */
    override fun <R> tryDeserialization(
        value: String,
        target: Class<R>
    ): Result<R, Fail.Incident.Transform.Deserialization> = try {
        success(mapper.readValue(value, target))
    } catch (expected: Exception) {
        failure(
            Fail.Incident.Transform.Deserialization(description = "Error of deserialization.", exception = expected)
        )
    }

    override fun <R> tryDeserialization(
        value: String,
        typeRef: TypeReference<R>
    ): Result<R, Fail.Incident.Transform.Deserialization> = try {
        success(mapper.readValue(value, typeRef))
    } catch (expected: Exception) {
        failure(
            Fail.Incident.Transform.Deserialization(description = "Error of deserialization.", exception = expected)
        )
    }

    /**
     * Serialization
     */
    override fun <R> trySerialization(value: R): Result<String, Fail.Incident.Transform.Serialization> = try {
        success(mapper.writeValueAsString(value))
    } catch (expected: Exception) {
        failure(Fail.Incident.Transform.Serialization(description = "Error of serialization.", exception = expected))
    }

    /**
     * ???
     */
    override fun tryToJson(value: JsonNode): Result<String, Fail.Incident.Transform.Serialization> = try {
        success(mapper.writeValueAsString(value))
    } catch (expected: Exception) {
        failure(Fail.Incident.Transform.Serialization(description = "Error of serialization.", exception = expected))
    }
}
