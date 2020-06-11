package com.procurement.qualification.lib

inline fun <T, V> Collection<T>.isUnique(selector: (T) -> V): Boolean {
    val unique = HashSet<V>()
    forEach { item ->
        if (!unique.add(selector(item))) return false
    }
    return true
}

inline fun <T, V> Collection<T>.toSetBy(selector: (T) -> V): Set<V> {
    val collections = LinkedHashSet<V>()
    forEach {
        collections.add(selector(it))
    }
    return collections
}

inline fun <T, C : Collection<T>, E : RuntimeException> C?.errorIfEmpty(exceptionBuilder: () -> E): C? =
    if (this != null && this.isEmpty())
        throw exceptionBuilder()
    else
        this
