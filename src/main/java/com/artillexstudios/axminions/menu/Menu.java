package com.artillexstudios.axminions.menu;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.utils.Cooldown;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axminions.minions.Minion;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class Menu {
    private final Cooldown<UUID> cooldown = new Cooldown<>();
    private final Config config;
    private final Minion minion;
    private final Gui gui;

    public Menu(Config config, Minion minion) {
        this.config = config;
        this.minion = minion;

        this.gui = Gui.gui()
                .title(StringUtils.format(config.getString("title")))
                .rows(config.getInt("rows"))
                .create();

        this.gui.setDefaultClickAction(event -> event.setCancelled(true));

        this.update();
    }

    public static IntArrayList slots(List<String> slotString) {
        IntArrayList returnedSlots = new IntArrayList();

        for (String s : slotString) {
            if (s.contains("-")) {
                String[] split = s.split("-");
                returnedSlots.addAll(getSlots(Integer.parseInt(split[0]), Integer.parseInt(split[1])));
            } else {
                returnedSlots.add(Integer.parseInt(s));
            }
        }

        return returnedSlots;
    }

    private static List<Integer> getSlots(int small, int max) {
        IntArrayList slots = new IntArrayList();

        for (int i = small; i <= max; i++) {
            slots.add(i);
        }
        return slots;
    }

    public void update() {
        List<Map<Object, Object>> items = config.getMapList("items");
        for (Map<Object, Object> item : items) {
            ItemBuilder builder = new ItemBuilder(item);
            List<String> slots = (List<String>) item.getOrDefault("slots", List.of());

            GuiItem guiItem = new GuiItem(builder.get(), event -> {
                if (cooldown.hasCooldown(event.getWhoClicked().getUniqueId())) {
                    event.setCancelled(true);
                    return;
                }

                if (!minion.ticking()) {
                    event.setCancelled(true);
                    return;
                }

                cooldown.addCooldown(event.getWhoClicked().getUniqueId(), 250);
            });
            
            slots(slots).intIterator().forEachRemaining(slot -> {
                this.gui.updateItem(slot, guiItem);
            });
        }
    }

    public void open(Player player) {

    }
}
