package com.artillexstudios.axminions.minions.actions.effects.implementation;

import com.artillexstudios.axapi.loot.LootContextParamSets;
import com.artillexstudios.axapi.loot.LootContextParams;
import com.artillexstudios.axapi.loot.LootParams;
import com.artillexstudios.axapi.loot.LootTables;
import com.artillexstudios.axapi.nms.NMSHandlers;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.effects.Effect;
import com.artillexstudios.axminions.utils.ItemCollection;
import com.artillexstudios.axminions.utils.LogUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Map;

public class DamageEntityEffect extends Effect<Entity, ItemCollection> {
    private static final Player dummyPlayer = NMSHandlers.getNmsHandler().dummyPlayer();

    public DamageEntityEffect(Map<Object, Object> configuration) {
        super(configuration);
    }

    @Override
    public ItemCollection run(Minion minion, Entity argument) {
        dummyPlayer.getInventory().setItemInMainHand(minion.tool());
        LootParams params = new LootParams.Builder(argument.getWorld())
                .withParameter(LootContextParams.THIS_ENTITY, argument)
                .withParameter(LootContextParams.ORIGIN, argument.getLocation())
                .withParameter(LootContextParams.DAMAGE_SOURCE, dummyPlayer)
                .withParameter(LootContextParams.ATTACKING_ENTITY, dummyPlayer)
                .withParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, dummyPlayer)
                .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, dummyPlayer)
                .build(LootContextParamSets.ENTITY);

        Collection<ItemStack> items = LootTables.entityLootTable(argument.getType())
                .randomItems(params);

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