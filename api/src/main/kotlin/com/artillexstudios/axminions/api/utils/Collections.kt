package com.artillexstudios.axminions.api.utils

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

inline fun <K, V> Map<K, V>.fastFor(action: (K, V) -> Unit) {
    if (isEmpty()) return
    entries.forEach {
        action(it.key, it.value)
    }
}