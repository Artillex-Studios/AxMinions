package com.artillexstudios.axminions.minions.gui.actions;

import com.artillexstudios.axapi.context.HashMapContext;
import com.artillexstudios.axapi.gui.configuration.actions.Action;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.gui.MinionGui;
import com.artillexstudios.axminions.users.LinkData;
import com.artillexstudios.axminions.users.User;
import com.artillexstudios.axminions.users.Users;
import org.bukkit.entity.Player;

public final class LinkToContainerAction extends Action<String> {

    public LinkToContainerAction(String data) {
        super(data);
    }

    @Override
    public String transform(String data) {
        return data;
    }

    @Override
    public void run(Player player, HashMapContext context) {
        Minion minion = context.get(MinionGui.MINION);
        if (minion == null) {
            return;
        }

        User user = Users.get(player);
        if (user == null) {
            return;
        }

        user.linkData(new LinkData(minion));
    }
}
