package com.artillexstudios.axminions.minions

object MinionTicker {
    private var tick = 0L

    fun tickAll() {
        // Code to tick all

        tick++
    }

    fun getTick(): Long {
        return this.tick
    }
}