package com.artillexstudios.axminions.minions

import com.artillexstudios.axapi.scheduler.Scheduler
import com.artillexstudios.axminions.api.utils.fastFor

object MinionTicker {
    private var tick = 0L

    private inline fun tickAll() {
        Minions.get { minions ->
            minions.fastFor { pos ->
                pos.minions.fastFor {
                    it.tick()
                }
            }
        }

        tick++
    }

    fun startTicking() {
        Scheduler.get().runTimer({ _ ->
            tickAll()
        }, 1, 1)
    }

    fun getTick(): Long {
        return this.tick
    }
}