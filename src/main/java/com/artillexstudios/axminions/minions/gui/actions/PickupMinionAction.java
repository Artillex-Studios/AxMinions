package com.artillexstudios.axminions.minions.gui.actions;

import com.artillexstudios.axapi.context.HashMapContext;
import com.artillexstudios.axapi.gui.configuration.actions.Action;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.gui.MinionGui;
import org.bukkit.entity.Player;

public final class PickupMinionAction extends Action<String> {

    public PickupMinionAction(String data) {
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

        minion.remove();
    }
}
