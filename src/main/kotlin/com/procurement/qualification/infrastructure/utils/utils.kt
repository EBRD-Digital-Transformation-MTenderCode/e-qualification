package com.procurement.qualification.infrastructure.utils

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.infrastructure.bind.jackson.configuration
import com.procurement.qualification.infrastructure.fail.Fail
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

private object JsonMapper {
    val mapper: ObjectMapper = ObjectMapper().apply {
        configuration()
    }
}

/*Date utils*/
fun LocalDateTime.toDate(): Date {
    return Date.from(this.toInstant(ZoneOffset.UTC))
}

fun Date.toLocal(): LocalDateTime {
    return LocalDateTime.ofInstant(this.toInstant(), ZoneOffset.UTC)
}

/**
Json utils
 **/
fun <T : Any> T.toJson(): String = try {
    JsonMapper.mapper.writeValueAsString(this)
} catch (expected: JsonProcessingException) {
    val className = this::class.java.canonicalName
    throw IllegalArgumentException("Error mapping an object of type '$className' to JSON.", expected)
}

fun <T : Any> String.toObject(target: Class<T>): T = try {
    JsonMapper.mapper.readValue(this, target)
} catch (expected: Exception) {
    throw IllegalArgumentException("Error binding JSON to an object of type '${target.canonicalName}'.", expected)
}

fun <T : Any> String.tryToObject(target: Class<T>): Result<T, Fail.Incident.Transform.Parsing> = try {
    Result.success(JsonMapper.mapper.readValue(this, target))
} catch (expected: Exception) {
    Result.failure(Fail.Incident.Transform.Parsing(target.canonicalName, expected))
}

fun <T : Any> JsonNode.tryToObject(target: Class<T>): Result<T, Fail.Incident.Transform.Parsing> {
    if (this is NullNode) return Result.failure(Fail.Incident.Transform.Parsing(target.canonicalName))
    return try {
        Result.success(JsonMapper.mapper.treeToValue(this, target))
    } catch (expected: Exception) {
        Result.failure(Fail.Incident.Transform.Parsing(target.canonicalName, expected))
    }
}

fun String.tryToNode(): Result<JsonNode, Fail.Incident.Transform.Parsing> = try {
    Result.success(JsonMapper.mapper.readTree(this))
} catch (exception: JsonProcessingException) {
    Result.failure(Fail.Incident.Transform.Parsing(JsonNode::class.java.canonicalName, exception))
}
