package com.artillexstudios.axminions.minions.actions.filters.implementation;

import com.artillexstudios.axminions.minions.actions.filters.Filter;
import com.artillexstudios.axminions.minions.actions.filters.Transformer;
import com.artillexstudios.axminions.utils.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MaterialFilter extends Filter<Material> {
    private final HashSet<Material> allowed = new HashSet<>();

    public MaterialFilter(Map<Object, Object> configuration) {
        this.addTransformer(new Transformer<Location, Material>() {
            @Override
            public Material transform(Location object) {
                return object.getBlock().getType();
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

        this.addTransformer(new Transformer<Block, Material>() {
            @Override
            public Material transform(Block object) {
                return object.getType();
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
            Matcher matcher = pattern.matcher(m.name().toLowerCase(Locale.ENGLISH));
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
    public boolean isAllowed(Material object) {
        return this.allowed.contains(object);
    }

    @Override
    public Class<?> inputClass() {
        return Material.class;
    }
}
