package com.procurement.qualification.domain.util.extension

inline fun Boolean.ifFalse(block: () -> Nothing) {
    if (!this) block()
}