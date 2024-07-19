package com.artillexstudios.axminions.api.events;

import com.artillexstudios.axminions.minions.MinionType;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class MinionTypeLoadEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final MinionType type;

    public MinionTypeLoadEvent(MinionType type) {
        super(!Bukkit.isPrimaryThread());
        this.type = type;
    }

    public MinionType type() {
        return this.type;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
