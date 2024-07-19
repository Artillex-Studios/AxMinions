package com.artillexstudios.axminions.minions;

import com.artillexstudios.axminions.utils.LogUtils;
import org.bukkit.World;

import java.util.Collection;
import java.util.IdentityHashMap;

public final class MinionWorldCache {
    private static final IdentityHashMap<World, MinionArea> worlds = new IdentityHashMap<>();

    public static void loadArea(World world) {
        if (worlds.containsKey(world)) {
            LogUtils.warn("An area is already present for world {}", world.getName());
            return;
        }

        worlds.put(world, new MinionArea());
    }

    public static MinionArea getArea(World world) {
        return worlds.get(world);
    }

    public static MinionArea remove(World world) {
        return worlds.remove(world);
    }

    public static Collection<MinionArea> worlds() {
        return worlds.values();
    }
}