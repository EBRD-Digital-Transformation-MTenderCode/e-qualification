package com.procurement.qualification.domain.enums

import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.qualification.domain.exception.EnumElementProviderException
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.Result.Companion.failure
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.infrastructure.fail.Fail

enum class ProcurementMethod(@JsonValue val key: String) {
    MV("open"),
    OT("open"),
    RT("selective"),
    SV("open"),
    DA("limited"),
    NP("limited"),
    FA("limited"),
    OP("selective"),
    GPA("selective"),
    TEST_OT("open"),
    TEST_SV("open"),
    TEST_RT("selective"),
    TEST_MV("open"),
    TEST_DA("limited"),
    TEST_NP("limited"),
    TEST_FA("limited"),
    TEST_OP("selective"),
    TEST_GPA("selective");

    override fun toString(): String = key

    companion object {

        private val allowedValues = values()

        fun creator(name: String) = try {
            valueOf(name)
        } catch (ignored: Exception) {
            throw EnumElementProviderException(
                enumType = this::class.java.canonicalName,
                value = name,
                values = allowedValues.joinToString { it.name }
            )
        }

        fun tryOf(name: String): Result<ProcurementMethod, Fail.Incident.Transform.Parsing> = try {
            valueOf(name).asSuccess()
        } catch (exception: Exception) {
            failure(
                Fail.Incident.Transform.Parsing(
                    className = ProcurementMethod::class.java::getCanonicalName.toString(),
                    exception = exception
                )
            )
        }
    }
}

