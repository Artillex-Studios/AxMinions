package com.artillexstudios.axminions.api.utils

class CoolDown<T> {
    private val map = HashMap<T, Long>()

    fun add(value: T, time: Long) {
        expire()
        map[value] = System.currentTimeMillis() + time
    }

    fun contains(value: T): Boolean {
        expire()
        return map.containsKey(value)
    }

    fun remove(key: T): Long? {
        expire()
        return map.remove(key)
    }

    private fun expire() {
        val currentTime = System.currentTimeMillis()

        val iterator = map.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next.value <= currentTime) {
                iterator.remove()
            }
        }
    }
}