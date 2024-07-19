package com.artillexstudios.axminions.utils;

import org.bukkit.Location;

public final class LocationUtils {

    public static Location toCenterLocation(Location location) {
        Location clone = location.clone();
        clone.setX(clone.getBlockX() + 0.5);
        clone.setY(clone.getBlockY() + 0.5);
        clone.setZ(clone.getBlockZ() + 0.5);
        return clone;
    }

    public static Location toBlockCenter(Location location) {
        Location clone = location.clone();
        clone.setX(clone.getBlockX() + 0.5);
        clone.setZ(clone.getBlockZ() + 0.5);
        return clone;
    }
}
