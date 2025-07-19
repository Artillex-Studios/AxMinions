package com.artillexstudios.axminions.api.events;

import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.effects.Effect;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class EffectDispatchEvent extends MinionEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Effect<?, ?> effect;
    private final Object argument;

    public EffectDispatchEvent(Minion minion, Effect<?,?> effect, Object argument) {
        super(minion);
        this.effect = effect;
        this.argument = argument;
    }

    public Effect<?, ?> effect() {
        return this.effect;
    }

    public Object argument() {
        return this.argument;
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
