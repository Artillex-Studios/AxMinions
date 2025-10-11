package com.artillexstudios.axminions.minions.actions.effects.implementation;

import com.artillexstudios.axminions.exception.MinionTickFailException;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.effects.Effect;
import com.artillexstudios.axminions.utils.ItemCollection;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

import java.util.Map;

public final class CollectEffect extends Effect<Entity, ItemCollection> {

    public CollectEffect(Map<Object, Object> configuration) {
        super(configuration);
    }

    @Override
    public ItemCollection run(Minion minion, Entity argument) {
        if (!(argument instanceof Item item)) {
            throw new MinionTickFailException("Entity is not an item!");
        }

        ItemCollection collection = ItemCollection.of(item.getItemStack());
        item.remove();
        return collection;
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
