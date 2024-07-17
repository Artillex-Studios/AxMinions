package com.artillexstudios.axminions.minions;

import com.artillexstudios.axminions.minions.skins.Skin;
import com.artillexstudios.axminions.utils.CollectionUtils;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

// The data needed to spawn a minion
public record MinionData(int ownerId, MinionType type, Level level, ItemStack tool, Skin skin, HashMap<String, String> storage) {
    private static final Pattern SEMICOLON = Pattern.compile(";");
    private static final Pattern DASH = Pattern.compile("-");

    public MinionData withSkin(Skin skin) {
        return new MinionData(this.ownerId, this.type, this.level, this.tool, skin, this.storage);
    }

    public static String serialize(Map<String, String> map) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            builder.append(entry.getKey()).append('-').append(entry.getValue()).append(';');
        }

        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    public static Map<String, String> deserialize(String string) {
        if (string.isBlank() || !string.contains("-")) {
            return Map.of();
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
}
