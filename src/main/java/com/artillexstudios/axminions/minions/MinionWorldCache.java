package com.artillexstudios.axminions.minions;

import com.artillexstudios.axminions.utils.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.World;

import java.util.Collection;
import java.util.IdentityHashMap;

public final class MinionWorldCache {
    private static final IdentityHashMap<World, MinionArea> worlds = new IdentityHashMap<>();
    private static final ObjectArrayList<Minion> minions = new ObjectArrayList<>();

    public static void loadArea(World world) {
        if (worlds.containsKey(world)) {
            LogUtils.warn("An area is already present for world {}", world.getName());
            return;
        }

        worlds.put(world, new MinionArea());
    }

    public static void add(Minion minion) {
        minions.add(minion);
        MinionArea area = worlds.get(minion.location().getWorld());
        if (area == null) {
            LogUtils.error("Tried to add minion to unknown world! {}", minion);
            return;
        }

        area.load(minion);
    }

    public static void remove(Minion minion) {
        minions.remove(minion);
        MinionArea area = worlds.get(minion.location().getWorld());
        if (area == null) {
            LogUtils.error("Tried to remove minion from unknown world! {}", minion);
            return;
        }

        area.remove(minion);
    }

    public static MinionArea getArea(World world) {
        return worlds.get(world);
    }

    public static MinionArea remove(World world) {
        return worlds.remove(world);
    }

    public static void clear(World world) {
        LogUtils.debug("Worlds map pre clear: {}", worlds);
        MinionArea area = getArea(world);
        if (area == null) {
            LogUtils.error("Tried to remove minion from unknown world {}! Map: {}", world.getName(), worlds);
            return;
        }

        area.forEachPos(position -> {
            for (Minion minion : position.minions()) {
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
        return worlds.values();
    }
}