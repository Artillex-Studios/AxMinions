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

public record PlacedMinionData(Integer id, Integer ownerId, MinionType type, Direction direction, Location location,
                               Location linkedChest, Level level,
                               Long charge, ItemStack tool, Skin skin, HashMap<String, String> extraData) {

    public PlacedMinionData(Integer id, UUID worldUUID, Integer typeId, Integer locationX, Integer locationY, Integer locationZ, Integer ownerId, Integer level, Long charge, Integer facing, Blob tool, Blob extraData) throws SQLException, IOException {
        this(id, ownerId, MinionTypes.parse(typeId.shortValue()), Direction.values()[facing], new Location(Bukkit.getWorld(worldUUID), locationX + 0.5, locationY, locationZ + 0.5), null, MinionTypes.parse(typeId.shortValue()).level(level), charge, tool == null ? null : WrapperRegistry.ITEM_STACK.map(tool.getBinaryStream().readAllBytes()).toBukkit(), null, MinionData.deserialize(new String(extraData.getBinaryStream().readAllBytes(), StandardCharsets.UTF_8)));
    }

    public Minion create() {
        return new Minion(this.id, this.location, new MinionData(this.ownerId, this.type, this.direction, this.linkedChest, this.level, this.charge, this.tool, this.skin, this.extraData));
    }
}
