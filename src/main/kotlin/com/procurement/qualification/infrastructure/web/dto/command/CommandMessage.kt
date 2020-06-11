package com.procurement.qualification.infrastructure.web.dto.command

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.JsonNode
import com.procurement.qualification.application.model.parseCpid
import com.procurement.qualification.application.model.parseOcid
import com.procurement.qualification.domain.enums.EnumElementProvider
import com.procurement.qualification.domain.enums.ProcurementMethod
import com.procurement.qualification.domain.enums.Stage
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.Result.Companion.failure
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.infrastructure.fail.error.DataErrors
import com.procurement.qualification.infrastructure.web.dto.Action

data class CommandMessage @JsonCreator constructor(

    val id: String,
    val command: CommandType,
    val context: Context,
    val data: JsonNode,
    val version: ApiVersion
)

val CommandMessage.cpid: Result<Cpid, DataErrors>
    get() {
        val cpid = this.context.cpid
            ?: return failure(DataErrors.Validation.MissingRequiredAttribute(name = "cpid"))
        return parseCpid(cpid)
    }

val CommandMessage.ocid: Result<Ocid, DataErrors>
    get() {
        val ocid = this.context.ocid
            ?: return failure(DataErrors.Validation.MissingRequiredAttribute(name = "ocid"))
        return parseOcid(ocid)
    }

val CommandMessage.stage: Result<Stage, DataErrors>
    get() {
        val stageAttributeName = "stage"
        val stage = this.context.stage
            ?: return failure(DataErrors.Validation.MissingRequiredAttribute(name = stageAttributeName))
        return Stage.orNull(stage)?.asSuccess() ?: failure(
            DataErrors.Validation.UnknownValue(
                name = stageAttributeName,
                expectedValues = Stage.allowedElements.map { it.key },
                actualValue = stage
            )
        )
    }

val CommandMessage.country: Result<String, DataErrors>
    get() = this.context.country?.asSuccess()
        ?: failure(DataErrors.Validation.MissingRequiredAttribute(name = "country"))

val CommandMessage.pmd: Result<ProcurementMethod, DataErrors>
    get() {
        val pmdAttributeName = "pmd"
        val pmd = this.context.pmd
            ?: return failure(DataErrors.Validation.MissingRequiredAttribute(name = pmdAttributeName))
        return ProcurementMethod.tryOf(pmd)
            .doReturn {
                return failure(
                    DataErrors.Validation.UnknownValue(
                        name = pmdAttributeName,
                        expectedValues = ProcurementMethod.values().map { it.name },
                        actualValue = pmd
                    )
                )
            }.asSuccess()
    }

data class Context @JsonCreator constructor(
    val cpid: String?,
    val ocid: String?,
    val stage: String?,
    val country: String?,
    val pmd: String?
)

enum class CommandType(@JsonValue override val key: String) : Action, EnumElementProvider.Key {

    TODO(""); //TODO()

    override fun toString(): String = key

    companion object : EnumElementProvider<CommandType>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = CommandType.orThrow(name)
    }
}

enum class ApiVersion(private val value: String) {
    V_0_0_1("0.0.1");

    @JsonValue
    fun value(): String {
        return this.value
    }

    override fun toString(): String {
        return this.value
    }
}