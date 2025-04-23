package com.artillexstudios.axminions.minions;

import com.artillexstudios.axapi.nms.wrapper.WrapperRegistry;
import com.artillexstudios.axminions.minions.skins.Skin;
import com.artillexstudios.axminions.utils.Direction;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public record PlacedMinionData(int id, int ownerId, MinionType type, Direction direction, Location location,
                               Location linkedChest, Level level,
                               long charge, ItemStack tool, Skin skin, HashMap<String, String> extraData) {

    public PlacedMinionData(int id, UUID worldUUID, int typeId, int locationX, int locationY, int locationZ, int ownerId, int level, long charge, int facing, Blob tool, Blob extraData) throws SQLException, IOException {
        this(id, ownerId, MinionTypes.parse((short) typeId), Direction.values()[facing], new Location(Bukkit.getWorld(worldUUID), locationX + 0.5, locationY, locationZ + 0.5), null, MinionTypes.parse((short) typeId).level(level), charge, tool == null ? null : WrapperRegistry.ITEM_STACK.map(tool.getBinaryStream().readAllBytes()).toBukkit(), null, MinionData.deserialize(new String(extraData.getBinaryStream().readAllBytes(), StandardCharsets.UTF_8)));
    }

    public Minion create() {
        return new Minion(this.id, this.location, new MinionData(this.ownerId, this.type, this.direction, this.linkedChest, this.level, this.charge, this.tool, this.skin, this.extraData));
    }
}
