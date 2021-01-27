package com.procurement.qualification.application.model

import com.procurement.qualification.domain.enums.CriteriaRelatesTo
import com.procurement.qualification.domain.enums.EnumElementProvider
import com.procurement.qualification.domain.enums.EnumElementProvider.Companion.keysAsStrings
import com.procurement.qualification.domain.enums.OperationType
import com.procurement.qualification.domain.enums.ProcurementMethodDetails
import com.procurement.qualification.domain.enums.RequirementDataType
import com.procurement.qualification.domain.enums.RequirementStatus
import com.procurement.qualification.domain.fail.error.DataTimeError
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.domain.model.Owner
import com.procurement.qualification.domain.model.qualification.QualificationId
import com.procurement.qualification.domain.model.requirement.RequirementId
import com.procurement.qualification.domain.model.submission.SubmissionId
import com.procurement.qualification.domain.model.tryOwner
import com.procurement.qualification.domain.util.extension.tryParseLocalDateTime
import com.procurement.qualification.infrastructure.fail.error.DataErrors
import java.time.LocalDateTime

fun parseCpid(value: String): Result<Cpid, DataErrors.Validation.DataMismatchToPattern> =
    Cpid.tryCreateOrNull(value = value)
        ?.asSuccess()
        ?: Result.failure(
            DataErrors.Validation.DataMismatchToPattern(
                name = "cpid",
                pattern = Cpid.pattern,
                actualValue = value
            )
        )

fun parseOcid(value: String): Result<Ocid, DataErrors.Validation.DataMismatchToPattern> =
    Ocid.tryCreateOrNull(value = value)
        ?.asSuccess()
        ?: Result.failure(
            DataErrors.Validation.DataMismatchToPattern(
                name = "ocid",
                pattern = Ocid.pattern,
                actualValue = value
            )
        )

fun parsePmd(
    value: String, allowedEnums: List<ProcurementMethodDetails>, attributeName: String = "pmd"
): Result<ProcurementMethodDetails, DataErrors> =
    parseEnum(value = value, allowedEnums = allowedEnums, attributeName = attributeName, target = ProcurementMethodDetails)

fun parseOperationType(
    value: String, allowedEnums: List<OperationType>, attributeName: String = "operationType"
): Result<OperationType, DataErrors> =
    parseEnum(value = value, allowedEnums = allowedEnums, attributeName = attributeName, target = OperationType)

fun parseRequirementStatus(
    value: String, allowedEnums: Set<RequirementStatus>, attributeName: String
): Result<RequirementStatus, DataErrors> =
    parseEnum(value = value, allowedEnums = allowedEnums, attributeName = attributeName, target = RequirementStatus)

fun parseDataType(
    value: String, allowedEnums: Set<RequirementDataType>, attributeName: String
): Result<RequirementDataType, DataErrors> =
    parseEnum(value = value, allowedEnums = allowedEnums, attributeName = attributeName, target = RequirementDataType)

fun parseCriteriaRelatesTo(
    value: String, allowedEnums: Set<CriteriaRelatesTo>, attributeName: String
): Result<CriteriaRelatesTo, DataErrors> =
    parseEnum(value = value, allowedEnums = allowedEnums, attributeName = attributeName, target = CriteriaRelatesTo)

fun <T> parseEnum(
    value: String, allowedEnums: Collection<T>, attributeName: String, target: EnumElementProvider<T>
): Result<T, DataErrors.Validation.UnknownValue> where T : Enum<T>,
                                                              T : EnumElementProvider.Key {
    val allowed = allowedEnums.toSet()
    return target.orNull(value)
        ?.takeIf { it in allowed }
        ?.asSuccess()
        ?: Result.failure(
            DataErrors.Validation.UnknownValue(
                name = attributeName,
                expectedValues = allowed.keysAsStrings(),
                actualValue = value
            )
        )
}


fun parseDate(value: String, attributeName: String): Result<LocalDateTime, DataErrors.Validation> =
    value.tryParseLocalDateTime()
        .mapError { fail ->
            when (fail) {
                is DataTimeError.InvalidFormat -> DataErrors.Validation.DataFormatMismatch(
                    name = attributeName,
                    actualValue = value,
                    expectedFormat = fail.pattern
                )

                is DataTimeError.InvalidDateTime ->
                    DataErrors.Validation.InvalidDateTime(name = attributeName, actualValue = value)
            }
        }

fun parseOwner(value: String): Result<Owner, DataErrors.Validation.DataFormatMismatch> =
    value.tryOwner()
        .doReturn {
            return Result.failure(
                DataErrors.Validation.DataFormatMismatch(
                    name = "owner",
                    actualValue = value,
                    expectedFormat = "uuid"
                )
            )
        }
        .asSuccess()

fun parseQualificationId(value: String): Result<QualificationId, DataErrors.Validation.DataFormatMismatch> {
    val id = QualificationId.tryCreateOrNull(text = value)
        ?: return Result.failure(
            DataErrors.Validation.DataFormatMismatch(
                name = "id",
                actualValue = value,
                expectedFormat = QualificationId.pattern
            )
        )
    return id.asSuccess()
}

fun parseSubmissionId(value: String): Result<SubmissionId, DataErrors.Validation.DataFormatMismatch> {
    val id = SubmissionId.tryCreateOrNull(text = value)
        ?: return Result.failure(
            DataErrors.Validation.DataFormatMismatch(
                name = "submissionId",
                actualValue = value,
                expectedFormat = SubmissionId.pattern
            )
        )
    return id.asSuccess()
}

fun parseRequirementId(value: String, attributeName: String): Result<RequirementId, DataErrors.Validation.DataFormatMismatch> {
    val id = RequirementId.parse(text = value)
        ?: return Result.failure(
            DataErrors.Validation.DataFormatMismatch(
                name = attributeName,
                actualValue = value,
                expectedFormat = "string"
            )
        )
    return id.asSuccess()
}

