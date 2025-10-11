package com.artillexstudios.axminions.minions.actions.effects.implementation;

import com.artillexstudios.axapi.loot.LootContextParamSets;
import com.artillexstudios.axapi.loot.LootContextParams;
import com.artillexstudios.axapi.loot.LootParams;
import com.artillexstudios.axapi.loot.LootTables;
import com.artillexstudios.axapi.nms.NMSHandlers;
import com.artillexstudios.axapi.nms.wrapper.ServerPlayerWrapper;
import com.artillexstudios.axminions.config.Config;
import com.artillexstudios.axminions.integrations.Integrations;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.effects.Effect;
import com.artillexstudios.axminions.utils.ItemCollection;
import org.bukkit.entity.Entity;

import java.util.Map;

public final class DamageEntityEffect extends Effect<Entity, ItemCollection> {
    private static final ServerPlayerWrapper dummyPlayer = NMSHandlers.getNmsHandler().dummyPlayer();

    public DamageEntityEffect(Map<Object, Object> configuration) {
        super(configuration);
    }

    @Override
    public ItemCollection run(Minion minion, Entity argument) {
        if (Config.instantKill) {
            // TODO: maybe we can get the loot of an entity with the stacker's api
            long stackSize = Integrations.STACKER.getStackSize(argument);

            dummyPlayer.wrapped().getInventory().setItemInMainHand(minion.tool());
            // TODO: We need to handle looting enchantment
            LootParams params = new LootParams.Builder(argument.getWorld())
                    .withParameter(LootContextParams.THIS_ENTITY, argument)
                    .withParameter(LootContextParams.ORIGIN, argument.getLocation())
                    .withParameter(LootContextParams.DAMAGE_SOURCE, dummyPlayer.wrapped())
                    .withParameter(LootContextParams.ATTACKING_ENTITY, dummyPlayer.wrapped())
                    .withParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, dummyPlayer.wrapped())
                    .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, dummyPlayer.wrapped())
                    .build(LootContextParamSets.ENTITY);

            return new ItemCollection(LootTables.entityLootTable(argument.getType())
                    .randomItems(params));
        }

        return ItemCollection.EMPTY;
    }

    @Override
    public boolean validate(Entity input) {
        return input != null;
    }

    @Override
    public Class<Entity> inputClass() {
        return Entity.class;
    }

    @Override
    public Class<ItemCollection> outputClass() {
        return ItemCollection.class;
    }
}
