package com.artillexstudios.axminions.minions;

import com.artillexstudios.axapi.libs.snakeyaml.external.biz.base64Coder.Base64Coder;
import com.artillexstudios.axapi.nms.wrapper.WrapperRegistry;
import com.artillexstudios.axminions.minions.skins.Skin;
import com.artillexstudios.axminions.utils.Direction;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public record PlacedMinionData(int id, int ownerId, MinionType type, Direction direction, Location location,
                               Location linkedChest, Level level,
                               long charge, ItemStack tool, Skin skin, HashMap<String, String> extraData) {

    public PlacedMinionData(int id, UUID worldUUID, short typeId, int locationX, int locationY, int locationZ, int ownerId, int level, long charge, byte facing, byte[] tool, byte[] extraData) {
        this(id, ownerId, MinionTypes.parse(typeId), Direction.values()[facing], new Location(Bukkit.getWorld(worldUUID), locationX, locationY, locationZ), null, MinionTypes.parse(typeId).level(level), charge, tool == null ? null : WrapperRegistry.ITEM_STACK.map(tool).toBukkit(), null, MinionData.deserialize(Base64Coder.encodeLines(extraData)));
    }

    public Minion create() {
        return new Minion(this.id, this.location, new MinionData(this.ownerId, this.type, this.direction, this.linkedChest, this.level, this.charge, this.tool, this.skin, this.extraData));
    }
}
