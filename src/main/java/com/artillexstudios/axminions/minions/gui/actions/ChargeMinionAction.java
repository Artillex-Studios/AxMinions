package com.artillexstudios.axminions.minions.gui.actions;

import com.artillexstudios.axapi.context.HashMapContext;
import com.artillexstudios.axapi.gui.configuration.actions.Action;
import org.bukkit.entity.Player;

public final class ChargeMinionAction extends Action<String> {

    public ChargeMinionAction(String data) {
        super(data);
    }

    @Override
    public String transform(String data) {
        return data;
    }

    @Override
    public void run(Player player, HashMapContext context) {

    }
}
