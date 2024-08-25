package com.artillexstudios.axminions.menu.actions;

import com.artillexstudios.axminions.menu.actions.implementation.ActionCharge;
import com.artillexstudios.axminions.menu.actions.implementation.ActionClose;
import com.artillexstudios.axminions.menu.actions.implementation.ActionConsoleCommand;
import com.artillexstudios.axminions.menu.actions.implementation.ActionMessage;
import com.artillexstudios.axminions.menu.actions.implementation.ActionPlayerCommand;
import com.artillexstudios.axminions.minions.Minion;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public final class Actions {
    private static final HashMap<String, Action> ACTIONS = new HashMap<>();
    private static final Action CHARGE = register(new ActionCharge());
    private static final Action CLOSE_MENU = register(new ActionClose());
    private static final Action CONSOLE_COMMAND = register(new ActionConsoleCommand());
    private static final Action PLAYER_COMMAND = register(new ActionPlayerCommand());
    private static final Action SEND_MESSAGE = register(new ActionMessage());

    public static Action register(Action action) {
        ACTIONS.put(action.getId(), action);
        return action;
    }

    public static void run(Player player, Minion minion, List<String> actions) {
        for (String rawAction : actions) {
            String id = StringUtils.substringBetween(rawAction, "[", "]").toLowerCase(Locale.ENGLISH);
            String arguments = StringUtils.substringAfter(rawAction, "] ");

            Action action = ACTIONS.get(id);
            if (action == null) {
                continue;
            }

            action.run(player, minion, arguments);
        }
    }
}
