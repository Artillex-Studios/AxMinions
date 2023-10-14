package com.artillexstudios.axminions.api.utils

class CoolDown<T> : HashMap<T, Long>() {

    fun add(value: T, time: Long) {
        expire()
        put(value, System.currentTimeMillis() + time)
    }

    override fun containsKey(key: T): Boolean {
        expire()
        return super.containsKey(key)
    }


    fun contains(value: T): Boolean {
        return containsKey(value)
    }

    override fun remove(key: T): Long? {
        expire()
        return super.remove(key)
    }

    private fun expire() {
        val currentTime = System.currentTimeMillis()

        val iterator = iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next.value <= currentTime) {
                iterator.remove()
            }
        }
    }
}