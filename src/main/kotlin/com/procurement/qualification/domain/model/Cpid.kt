package com.procurement.qualification.domain.model

import com.fasterxml.jackson.annotation.JsonValue


class Cpid private constructor(private val value: String) {

    override fun equals(other: Any?): Boolean {
        return if (this !== other)
            other is Cpid
                && this.value == other.value
        else
            true
    }

    override fun hashCode(): Int = value.hashCode()

    @JsonValue
    override fun toString(): String = value

    companion object {
        private val regex = "^[a-z]{4}-[a-z0-9]{6}-[A-Z]{2}-[0-9]{13}\$".toRegex()

        val pattern: String
            get() = regex.pattern

        fun tryCreateOrNull(value: String): Cpid? = if (value.matches(regex)) Cpid(value = value) else null
    }
}
