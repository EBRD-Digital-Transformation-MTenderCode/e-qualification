package com.procurement.qualification.application.model

import com.procurement.qualification.domain.functional.ValidationResult
import com.procurement.qualification.domain.functional.ValidationRule
import com.procurement.qualification.domain.util.extension.getDuplicate
import com.procurement.qualification.infrastructure.fail.error.DataErrors

fun <A : Any, T : Collection<A>?> noDuplicatesRule(
    attributeName: String,
    transform: (A) -> Any
): ValidationRule<T, DataErrors.Validation> =
    ValidationRule { received: T ->
        if (received == null) ValidationResult.ok()
        else {
            val transformed = mutableListOf<Any>()
            for (element in received)
                transformed.add(transform(element))
            val duplicate = transformed.getDuplicate { it }
            if (duplicate != null)
                ValidationResult.error(
                    DataErrors.Validation.UniquenessDataMismatch(
                        value = duplicate.toString(), name = attributeName
                    )
                )
            else
                ValidationResult.ok()
        }
    }

fun <T : Collection<Any>?> notEmptyRule(attributeName: String): ValidationRule<T, DataErrors.Validation> =
    ValidationRule { received: T ->
        if (received != null && received.isEmpty())
            ValidationResult.error(DataErrors.Validation.EmptyArray(attributeName))
        else
            ValidationResult.ok()
    }