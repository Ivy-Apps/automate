package automate

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull

fun <T> Collection<T>.toNonEmptyList(): NonEmptyList<T> =
    toNonEmptyListOrNull() ?: error("Impossible! This must be a non-empty list.")