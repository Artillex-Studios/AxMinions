package com.artillexstudios.axminions.minions.actions.filters.implementation;

import com.artillexstudios.axapi.config.adapters.MapConfigurationGetter;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axminions.exception.TransformerNotPresentException;
import com.artillexstudios.axminions.minions.actions.filters.Filter;
import com.artillexstudios.axminions.minions.actions.filters.Transformer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Tameable;

public final class TamedFilter extends Filter<Entity> {

    public TamedFilter(MapConfigurationGetter configuration) {
        this.addTransformer(Entity.class, new Transformer<Entity, Entity>() {
            @Override
            public Entity transform(Object object) {
                return (Entity) object;
            }

            @Override
            public Class<?> inputClass() {
                return Entity.class;
            }

            @Override
            public Class<?> outputClass() {
                return Entity.class;
            }
        });
    }

    @Override
    public boolean isAllowed(Object object) {
        try {
            Transformer<?, Entity> transformer = this.transformer(object.getClass());
            Entity transformed = transformer.transform(object);
            return transformed instanceof Tameable tameable && tameable.isTamed();
        } catch (TransformerNotPresentException exception) {
            LogUtils.error("No transformer found for input class {}!", object.getClass());
            return false;
        }
    }
}