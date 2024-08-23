package com.artillexstudios.axminions.minions;

import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.items.component.DataComponents;
import com.artillexstudios.axapi.items.nbt.CompoundTag;
import com.artillexstudios.axminions.minions.skins.Skin;
import com.artillexstudios.axminions.minions.skins.SkinRegistry;
import com.artillexstudios.axminions.users.User;
import com.artillexstudios.axminions.utils.CollectionUtils;
import com.artillexstudios.axminions.utils.Direction;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.joml.Math;

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
        HashMap<String, String> map = CollectionUtils.hashMap(split.length);
        for (String value : split) {
            String[] keyValue = DASH.split(value);
            if (keyValue.length != 2) continue;
            map.put(keyValue[0], keyValue[1]);
        }

        return map;
    }

    public static MinionData fromItem(User user, WrappedItemStack wrappedItemStack) {
        CompoundTag tag = wrappedItemStack.get(DataComponents.customData());
        String type = tag.getString("axminions_minion_type");
        int level = tag.getInt("axminions_minion_level");
        HashMap<String, String> extraData = MinionData.deserialize(tag.getString("axminions_minion_statistics"));
        String skin = tag.getString("axminions_minion_skin");
        long charge = tag.getLong("axminions_minion_charge");
        MinionType minionType = MinionTypes.parse(type);
        Level levelInstance = minionType.level(level);
        if (levelInstance == null) {
            levelInstance = minionType.level(Math.clamp(1, minionType.levels().size(), level));
        }

        return new MinionData(user.id(), minionType, Direction.NORTH, null, levelInstance, charge, new ItemStack(Material.AIR), SkinRegistry.parse(skin), extraData);
    }

    public MinionData withSkin(Skin skin) {
        return new MinionData(this.ownerId, this.type, this.direction, this.linkedChest, this.level, this.charge, this.tool, skin, this.extraData);
    }
}
