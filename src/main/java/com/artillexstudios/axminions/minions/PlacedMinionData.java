package com.artillexstudios.axminions.minions;

import com.artillexstudios.axminions.minions.skins.Skin;
import com.artillexstudios.axminions.utils.Direction;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public record PlacedMinionData(int ownerId, MinionType type, Direction direction, Location location,
                               Location linkedChest, Level level,
                               long charge, ItemStack tool, Skin skin, HashMap<String, String> extraData) {

    public Minion create() {
        return new Minion(this.location, new MinionData(this.ownerId, this.type, this.direction, this.linkedChest, this.level, this.charge, this.tool, this.skin, this.extraData));
    }
}
