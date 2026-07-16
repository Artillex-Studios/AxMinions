package com.artillexstudios.axminions.placeholders;

import com.artillexstudios.axapi.placeholders.PaperPlaceholderHandler;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.gui.MinionGui;

public final class Placeholders {

    public static void register() {
        PaperPlaceholderHandler.register("linked_chest_location", ctx -> {
            Minion minion = ctx.getByKey(MinionGui.MINION);
            if (minion == null) {
                return "";
            }

            // TODO: Prettier location
            return minion.linkedChest().toString();
        });
    }
}
