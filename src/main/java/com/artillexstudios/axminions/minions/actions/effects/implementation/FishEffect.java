package com.artillexstudios.axminions.minions.actions.effects.implementation;

import com.artillexstudios.axapi.loot.LootContextParamSets;
import com.artillexstudios.axapi.loot.LootContextParams;
import com.artillexstudios.axapi.loot.LootParams;
import com.artillexstudios.axapi.loot.LootTables;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.effects.Effect;
import com.artillexstudios.axminions.utils.ItemCollection;
import net.kyori.adventure.key.Key;
import org.bukkit.Location;

import java.util.Map;

public final class FishEffect extends Effect<Location, ItemCollection> {

    public FishEffect(Map<Object, Object> configuration) {
        super(configuration);
    }

    @Override
    public ItemCollection run(Minion minion, Location argument) {
        LootParams params = new LootParams.Builder(argument.getWorld())
                .withParameter(LootContextParams.ORIGIN, argument)
                .withParameter(LootContextParams.TOOL, minion.tool())
                .withOptionalParameter(LootContextParams.THIS_ENTITY, null)
                .build(LootContextParamSets.FISHING);

        return new ItemCollection(LootTables.lootTable(Key.key("minecraft", "gameplay/fishing"))
                .randomItems(params)
        );
    }

    @Override
    public boolean validate(Location input) {
        return input != null;
    }

    @Override
    public Class<Location> inputClass() {
        return Location.class;
    }

    @Override
    public Class<ItemCollection> outputClass() {
        return ItemCollection.class;
    }
}
