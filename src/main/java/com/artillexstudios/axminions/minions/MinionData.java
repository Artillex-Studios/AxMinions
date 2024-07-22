package com.artillexstudios.axminions.minions;

import com.artillexstudios.axminions.minions.skins.Skin;
import com.artillexstudios.axminions.utils.CollectionUtils;
import com.artillexstudios.axminions.utils.Direction;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

// The data needed to spawn a minion
public record MinionData(int ownerId, MinionType type, Direction direction, Location linkedChest, Level level,
                         long charge, ItemStack tool, Skin skin, HashMap<String, String> extraData) {
    private static final Pattern SEMICOLON = Pattern.compile(";");
    private static final Pattern DASH = Pattern.compile("-");

    public static String serialize(Map<String, String> map) {
        if (map.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            builder.append(entry.getKey()).append('-').append(entry.getValue()).append(';');
        }

        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    public static HashMap<String, String> deserialize(String string) {
        if (string.isEmpty()) {
            return new HashMap<>(0);
        }

        String[] split = SEMICOLON.split(string);
        Map<String, String> map = CollectionUtils.hashMap(split.length);
        for (String value : split) {
            String[] keyValue = DASH.split(value);
            if (keyValue.length != 2) continue;
            map.put(keyValue[0], keyValue[1]);
        }

        return map;
    }

    public MinionData withSkin(Skin skin) {
        return new MinionData(this.ownerId, this.type, this.direction, this.linkedChest, this.level, this.charge, this.tool, skin, this.extraData);
    }
}
