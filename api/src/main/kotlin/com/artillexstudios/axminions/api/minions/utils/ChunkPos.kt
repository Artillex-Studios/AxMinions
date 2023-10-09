package com.artillexstudios.axminions.api.minions.utils

data class ChunkPos(var x: Int, var z: Int) {

    fun clone(): ChunkPos {
        return ChunkPos(x, z)
    }
}