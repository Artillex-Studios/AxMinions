package com.artillexstudios.axminions.api.utils

import java.util.Collections
import java.util.Queue

inline fun <T> Array<T>.fastFor(action: (T) -> Unit) {
    val indices = indices
    for (i in indices) {
        action(get(i))
    }
}

inline fun <T> List<T>.fastFor(action: (T) -> Unit) {
    if (isEmpty()) return
    val indices = indices
    for (i in indices) {
        action(get(i))
    }
}

inline fun <T> Collection<T>.fastFor(action: (T) -> Unit) {
    if (isEmpty()) return
    val indices = indices

    for (i in indices) {
        action(elementAt(i))
    }
}

inline fun <K, V> Map<K, V>.fastFor(action: (K, V) -> Unit) {
    if (isEmpty()) return
    entries.fastFor {
        action(it.key, it.value)
    }
}