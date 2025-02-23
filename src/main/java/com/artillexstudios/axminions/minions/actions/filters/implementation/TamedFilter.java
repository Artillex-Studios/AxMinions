package com.artillexstudios.axminions.minions.actions.filters.implementation;

import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axminions.exception.TransformerNotPresentException;
import com.artillexstudios.axminions.minions.actions.filters.Filter;
import com.artillexstudios.axminions.minions.actions.filters.Transformer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Tameable;

import java.util.List;
import java.util.Map;

public final class TamedFilter extends Filter<Entity> {

    public TamedFilter(Map<Object, Object> configuration) {
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
            Transformer<?, Entity> transformer = transformer(object.getClass());
            Entity transformed = transformer.transform(object);
            return transformed instanceof Tameable tameable && tameable.isTamed();
        } catch (TransformerNotPresentException exception) {
            LogUtils.error("No transformer found for input class {}!");
            return false;
        }
    }

    @Override
    public List<Class<?>> inputClasses() {
        return List.of(Entity.class);
    }
}