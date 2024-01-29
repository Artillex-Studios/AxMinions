package com.artillexstudios.axminions.cache

@JvmRecord
data class ChunkLoc(val x: Int, val z: Int) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChunkLoc) return false

        if (x != other.x) return false
        if (z != other.z) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + z
        return result
    }
}