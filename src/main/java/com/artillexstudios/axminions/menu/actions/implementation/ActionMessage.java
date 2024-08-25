package com.artillexstudios.axminions.menu.actions.implementation;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axminions.menu.actions.Action;
import com.artillexstudios.axminions.minions.Minion;
import org.bukkit.entity.Player;

public final class ActionMessage extends Action {

    public ActionMessage() {
        super("message");
    }

    @Override
    public void run(Player player, Minion minion, String arguments) {
        player.sendMessage(StringUtils.formatToString(arguments));
    }
}
