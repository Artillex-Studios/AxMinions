package com.artillexstudios.axminions.api.utils

inline fun <T> Array<T>.fastFor(action: (T) -> Unit) {
    val indices = indices
    for (i in indices) {
        action(get(i))
    }
}

inline fun <T> List<T>.fastFor(action: (T) -> Unit) {
    val indices = indices
    for (i in indices) {
        action(get(i))
    }
}