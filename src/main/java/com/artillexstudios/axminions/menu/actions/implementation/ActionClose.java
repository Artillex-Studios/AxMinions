package com.artillexstudios.axminions.menu.actions.implementation;

import com.artillexstudios.axminions.menu.actions.Action;
import com.artillexstudios.axminions.minions.Minion;
import org.bukkit.entity.Player;

public final class ActionClose extends Action {

    public ActionClose() {
        super("close");
    }

    @Override
    public void run(Player player, Minion minion, String arguments) {
        player.closeInventory();
    }
}
