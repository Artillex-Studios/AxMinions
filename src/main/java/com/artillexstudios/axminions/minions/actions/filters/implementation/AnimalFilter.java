package com.artillexstudios.axminions.minions.actions.filters.implementation;

import com.artillexstudios.axapi.collections.IdentityArrayMap;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axminions.exception.TransformerNotPresentException;
import com.artillexstudios.axminions.minions.actions.filters.Filter;
import com.artillexstudios.axminions.minions.actions.filters.Transformer;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AnimalFilter extends Filter<EntityType> {
    private final Set<EntityType> allowed = Collections.newSetFromMap(new IdentityArrayMap<>());

    public AnimalFilter(Map<Object, Object> configuration) {
        this.addTransformer(Entity.class, new Transformer<Entity, EntityType>() {
            @Override
            public EntityType transform(Object object) {
                return ((Entity) object).getType();
            }

            @Override
            public Class<?> inputClass() {
                return Entity.class;
            }

            @Override
            public Class<?> outputClass() {
                return EntityType.class;
            }
        });

        this.addTransformer(EntityType.class, new Transformer<EntityType, EntityType>() {
            @Override
            public EntityType transform(Object object) {
                return (EntityType) object;
            }

            @Override
            public Class<?> inputClass() {
                return EntityType.class;
            }

            @Override
            public Class<?> outputClass() {
                return EntityType.class;
            }
        });

        for (EntityType entityType : EntityType.values()) {
            Class<?> entityClass = entityType.getEntityClass();
            if (entityClass == null) {
                continue;
            }

            if (Animals.class.isAssignableFrom(entityClass)) {
                this.allowed.add(entityType);
            }
        }
    }

    @Override
    public boolean isAllowed(Object object) {
        try {
            Transformer<?, EntityType> transformer = this.transformer(object.getClass());
            EntityType transformed = transformer.transform(object);
            return this.allowed.contains(transformed);
        } catch (TransformerNotPresentException exception) {
            LogUtils.error("No transformer found for input class {}!", object.getClass());
            return false;
        }
    }

    @Override
    public List<Class<?>> inputClasses() {
        return List.of(Entity.class, EntityType.class);
    }
}