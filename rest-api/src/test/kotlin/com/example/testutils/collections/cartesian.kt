package com.example.testutils.collections

infix fun <T, O> List<T>.cartesianProduct(others: List<O>): List<Pair<T, O>> =
        flatMap { t: T ->
            others.map { o -> Pair(t, o) }
        }

inline fun <T, O, R> List<T>.mapCartesianProduct(others: List<O>, transform: (Pair<T, O>) -> R): List<R> =
        flatMap { t: T ->
            others.map { o -> transform(Pair(t, o)) }
        }
