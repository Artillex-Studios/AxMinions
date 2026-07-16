package com.artillexstudios.axminions.minions.gui;

import com.artillexstudios.axapi.config.YamlConfiguration;
import com.artillexstudios.axapi.context.ContextKey;
import com.artillexstudios.axapi.gui.configuration.ConfigurationBackedGui;
import com.artillexstudios.axapi.gui.configuration.ConfigurationBackedGuiBuilder;
import com.artillexstudios.axapi.gui.configuration.actions.Actions;
import com.artillexstudios.axapi.gui.inventory.Gui;
import com.artillexstudios.axminions.AxMinionsPlugin;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.gui.actions.ChargeMinionAction;
import com.artillexstudios.axminions.minions.gui.actions.PickupMinionAction;
import com.artillexstudios.axminions.utils.FileUtils;

public final class MinionGui {
    public static ContextKey<Minion> MINION = new ContextKey<>("minion", Minion.class);
    private static final YamlConfiguration<?> MINION_CONFIG = YamlConfiguration.of(FileUtils.PLUGIN_DIRECTORY.resolve("guis").resolve("minion.yml"))
            .withDefaults(AxMinionsPlugin.instance().getResource("guis/minion.yml"))
            .build();

    public static final ConfigurationBackedGui<Gui> GUI;

    static {
        Actions.INSTANCE.register("charge", ChargeMinionAction::new);
        Actions.INSTANCE.register("pickup", PickupMinionAction::new);
        MINION_CONFIG.load();
        GUI = ConfigurationBackedGuiBuilder.builder(MINION_CONFIG, false).build();
    }
}
