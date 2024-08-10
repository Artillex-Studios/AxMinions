package com.artillexstudios.axminions.menu;

import com.artillexstudios.axminions.minions.Minion;
import dev.triumphteam.gui.guis.Gui;

public final class Menu {
    private final Minion minion;
    private final Gui gui;

    public Menu(Minion minion) {
        this.minion = minion;

        this.gui = Gui.gui().create();
    }
}
