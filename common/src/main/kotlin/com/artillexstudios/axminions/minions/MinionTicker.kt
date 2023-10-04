package com.artillexstudios.axminions.minions

import com.artillexstudios.axapi.scheduler.Scheduler

object MinionTicker {
    private var tick = 0L

    fun tickAll() {
        // Code to tick all

        tick++
    }

    fun startTicking() {
        Scheduler.get().runTimer({ task ->

        }, 0, 0)
    }

    fun getTick(): Long {
        return this.tick
    }
}