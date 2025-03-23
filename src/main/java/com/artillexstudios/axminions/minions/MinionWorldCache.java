package com.artillexstudios.axminions.minions;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axminions.config.Config;
import com.artillexstudios.axminions.utils.ThreadUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.World;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;

public final class MinionWorldCache {
    private static final IdentityHashMap<World, MinionArea> worlds = new IdentityHashMap<>();
    private static final ObjectArrayList<Minion> minions = new ObjectArrayList<>();

    public static MinionArea loadArea(World world) {
        ThreadUtils.ensureMain("Area can't be loaded from off main!");
        if (worlds.containsKey(world)) {
            LogUtils.warn("An area is already present for world {}", world.getName());
            return null;
        }

        MinionArea area = new MinionArea();
        worlds.put(world, area);
        return area;
    }

    public static void add(Minion minion) {
        ThreadUtils.ensureMain("Minion can't be added from off main!");
        minions.add(minion);
        MinionArea area = worlds.get(minion.location().getWorld());
        if (area == null) {
            LogUtils.error("Tried to add minion to unknown world! {}", minion);
            return;
        }

        area.load(minion);
    }

    public static void addAll(List<Minion> list) {
        ThreadUtils.ensureMain("Minion can't be added from off main!");
        if (list.isEmpty()) {
            return;
        }

        minions.addAll(list);
        MinionArea area = worlds.get(list.getFirst().location().getWorld());
        if (area == null) {
            LogUtils.error("Tried to add minions to unknown world! {}", list);
            return;
        }

        area.loadAll(list);
    }

    public static void remove(Minion minion) {
        ThreadUtils.ensureMain("Minion can't be removed from off main!");
        minions.remove(minion);
        MinionArea area = worlds.get(minion.location().getWorld());
        if (area == null) {
            LogUtils.error("Tried to remove minion from unknown world! {}", minion);
            return;
        }

        area.remove(minion);
    }

    public static MinionArea getArea(World world) {
        ThreadUtils.ensureMain("Asynchronous area get!");
        return worlds.get(world);
    }

    public static MinionArea remove(World world) {
        ThreadUtils.ensureMain("Asynchronous area remove!");
        return worlds.remove(world);
    }

    public static void clear(World world) {
        ThreadUtils.ensureMain("Asynchronous area clear!");
        if (Config.debug) {
            LogUtils.debug("Worlds map pre clear: {}", worlds);
        }
        MinionArea area = getArea(world);
        if (area == null) {
            LogUtils.error("Tried to remove minion from unknown world {}! Map: {}", world.getName(), worlds);
            return;
        }

        area.forEachPos(position -> {
            for (Minion minion : position.minions()) {
                minions.remove(minion);
                minion.destroy();
            }
        });

        area.clear();
    }

    public static ObjectArrayList<Minion> minions() {
        return minions;
    }

    public static ObjectArrayList<Minion> copy() {
        return minions.clone();
    }

    public static Collection<MinionArea> worlds() {
        ThreadUtils.ensureMain("Asynchronous worlds get!");
        return worlds.values();
    }
}