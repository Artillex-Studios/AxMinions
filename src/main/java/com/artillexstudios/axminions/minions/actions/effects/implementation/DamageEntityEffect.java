package com.artillexstudios.axminions.minions.actions.effects.implementation;

import com.artillexstudios.axapi.nms.NMSHandlers;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.effects.Effect;
import com.artillexstudios.axminions.utils.ItemCollection;
import com.artillexstudios.axminions.utils.LogUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTables;

import java.util.Collection;
import java.util.Map;
import java.util.Random;

public class DamageEntityEffect extends Effect<Entity, ItemCollection> {
    private static final Player dummyPlayer = NMSHandlers.getNmsHandler().dummyPlayer();

    public DamageEntityEffect(Map<Object, Object> configuration) {
        super(configuration);
    }

    @Override
    public ItemCollection run(Minion minion, Entity argument) {
        Collection<ItemStack> items = LootTables.ZOMBIE.getLootTable().populateLoot(new Random(), new LootContext.Builder(argument.getLocation())
                .killer(dummyPlayer)
                .lootedEntity(argument)
                .build()
        );
        LogUtils.debug("DamageEntityEffect, {} items: {}", argument, items);
        return new ItemCollection(items);
    }

    @Override
    public Class<?> inputClass() {
        return Entity.class;
    }

    @Override
    public Class<?> outputClass() {
        return ItemCollection.class;
    }
}
