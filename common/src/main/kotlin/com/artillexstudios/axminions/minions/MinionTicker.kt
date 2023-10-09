package com.artillexstudios.axminions.minions

import com.artillexstudios.axapi.scheduler.Scheduler
import com.artillexstudios.axminions.utils.fastFor

object MinionTicker {
    private var tick = 0L

    private inline fun tickAll() {
        Minions.get().values.forEach { list ->
            list.fastFor {
                it.tick()
            }
        }

        tick++
    }

    fun startTicking() {
        Scheduler.get().runTimer({ _ ->
            tickAll()
        }, 0, 0)
    }

    fun getTick(): Long {
        return this.tick
    }
}