package com.artillexstudios.axminions.api.events

import com.artillexstudios.axminions.api.minions.Minion
import org.bukkit.event.Event

abstract class MinionEvent(val minion: Minion) : Event()