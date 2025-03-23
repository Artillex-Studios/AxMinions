package com.artillexstudios.axminions.minions.actions.filters.implementation;

import com.artillexstudios.axapi.collections.IdentityArrayMap;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axminions.exception.TransformerNotPresentException;
import com.artillexstudios.axminions.minions.actions.filters.Filter;
import com.artillexstudios.axminions.minions.actions.filters.Transformer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MaterialFilter extends Filter<Material> {
    private final Set<Material> allowed = Collections.newSetFromMap(new IdentityArrayMap<>());

    public MaterialFilter(Map<Object, Object> configuration) {
        this.addTransformer(Location.class, new Transformer<Location, Material>() {
            @Override
            public Material transform(Object object) {
                return ((Location) object).getBlock().getType();
            }

            @Override
            public Class<?> inputClass() {
                return Location.class;
            }

            @Override
            public Class<?> outputClass() {
                return Material.class;
            }
        });

        this.addTransformer(Block.class, new Transformer<Block, Material>() {
            @Override
            public Material transform(Object object) {
                return ((Block) object).getType();
            }

            @Override
            public Class<?> inputClass() {
                return Block.class;
            }

            @Override
            public Class<?> outputClass() {
                return Material.class;
            }
        });

        this.addTransformer(Material.class, new Transformer<Material, Material>() {
            @Override
            public Material transform(Object object) {
                return (Material) object;
            }

            @Override
            public Class<?> inputClass() {
                return Material.class;
            }

            @Override
            public Class<?> outputClass() {
                return Material.class;
            }
        });

        List<String> whitelist = (List<String>) configuration.get("whitelist");
        if (whitelist != null) {
            for (String s : whitelist) {
                if (s.equals("*")) {
                    this.allowed.addAll(List.of(Material.values()));
                } else {
                    List<Material> materials = match(s);
                    if (materials == null) {
                        LogUtils.warn("No materials matching {} were found!", s);
                        continue;
                    }

                    this.allowed.addAll(materials);
                }
            }
        }

        List<String> blacklist = (List<String>) configuration.get("blacklist");
        if (blacklist != null) {
            for (String s : blacklist) {
                if (s.equals("*")) {
                    List.of(Material.values()).forEach(this.allowed::remove);
                } else {
                    List<Material> materials = match(s);
                    if (materials == null) {
                        LogUtils.warn("No materials matching {} were found!", s);
                        continue;
                    }

                    materials.forEach(this.allowed::remove);
                }
            }
        }
    }

    private static List<Material> match(String material) {
        ObjectArrayList<Material> materials = null;
        Pattern pattern = Pattern.compile(material, Pattern.CASE_INSENSITIVE);

        for (Material m : Material.values()) {
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
            Transformer<?, Material> transformer = this.transformer(object.getClass());
            Material transformed = transformer.transform(object);
            return this.allowed.contains(transformed);
        } catch (TransformerNotPresentException exception) {
            LogUtils.error("No transformer found for input class {}!", object.getClass());
            return false;
        }
    }

    @Override
    public List<Class<?>> inputClasses() {
        return List.of(Material.class, Location.class, Block.class);
    }
}
