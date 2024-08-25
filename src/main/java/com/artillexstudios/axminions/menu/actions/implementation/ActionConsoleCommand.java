package com.artillexstudios.axminions.menu.actions.implementation;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axminions.menu.actions.Action;
import com.artillexstudios.axminions.minions.Minion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class ActionConsoleCommand extends Action {

    public ActionConsoleCommand() {
        super("console");
    }

    @Override
    public void run(Player player, Minion minion, String arguments) {
        Scheduler.get().run(task -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), arguments.replace("%player%", player.getName()));
        });
    }
}
