package com.artillexstudios.axminions.minions.actions.filters.implementation;

import com.artillexstudios.axminions.exception.TransformerNotPresentException;
import com.artillexstudios.axminions.minions.actions.filters.Filter;
import com.artillexstudios.axminions.minions.actions.filters.Transformer;
import com.artillexstudios.axminions.utils.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EntityTypeFilter extends Filter<EntityType> {
    private final HashSet<EntityType> allowed = new HashSet<>();

    public EntityTypeFilter(Map<Object, Object> configuration) {
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

        List<String> whitelist = (List<String>) configuration.get("whitelist");
        if (whitelist != null) {
            for (String s : whitelist) {
                if (s.equals("*")) {
                    this.allowed.addAll(List.of(EntityType.values()));
                } else {
                    List<EntityType> entityTypes = match(s);
                    if (entityTypes == null) {
                        LogUtils.warn("No entitytype matching {} was found!", s);
                        continue;
                    }

                    this.allowed.addAll(entityTypes);
                }
            }
        }

        List<String> blacklist = (List<String>) configuration.get("blacklist");
        if (blacklist != null) {
            for (String s : blacklist) {
                if (s.equals("*")) {
                    List.of(EntityType.values()).forEach(this.allowed::remove);
                } else {
                    List<EntityType> entityTypes = match(s);
                    if (entityTypes == null) {
                        LogUtils.warn("No entitytype matching {} was found!", s);
                        continue;
                    }

                    entityTypes.forEach(this.allowed::remove);
                }
            }
        }
    }

    private static List<EntityType> match(String material) {
        ObjectArrayList<EntityType> materials = null;
        Pattern pattern = Pattern.compile(material, Pattern.CASE_INSENSITIVE);

        for (EntityType m : EntityType.values()) {
            Matcher matcher = pattern.matcher(m.name());
            if (!matcher.find()) {
                continue;
            }

            if (materials == null) {
                materials = new ObjectArrayList<>();
            }

            materials.add(m);
        }

        return materials;
    }

    @Override
    public boolean isAllowed(Object object) {
        try {
            Transformer<?, EntityType> transformer = transformer(object.getClass());
            EntityType transformed = transformer.transform(object);
            return this.allowed.contains(transformed);
        } catch (TransformerNotPresentException exception) {
            LogUtils.error("No transformer found for input class {}!");
            return false;
        }
    }

    @Override
    public List<Class<?>> inputClasses() {
        return List.of();
    }
}
