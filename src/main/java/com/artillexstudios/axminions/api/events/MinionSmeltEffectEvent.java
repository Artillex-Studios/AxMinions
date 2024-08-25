package com.artillexstudios.axminions.api.events;

import com.artillexstudios.axminions.minions.Minion;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MinionSmeltEffectEvent extends MinionEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final ItemStack from;
    private ItemStack to;

    public MinionSmeltEffectEvent(Minion minion, ItemStack from, ItemStack to) {
        super(minion);
        this.from = from;
        this.to = to;
    }

    @Nullable
    public ItemStack to() {
        return this.to;
    }

    public void setTo(ItemStack to) {
        this.to = to;
    }

    public ItemStack from() {
        return this.from;
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
