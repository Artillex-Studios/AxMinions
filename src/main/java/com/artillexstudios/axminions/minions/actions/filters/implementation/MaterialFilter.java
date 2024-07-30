package com.artillexstudios.axminions.minions.actions.filters.implementation;

import com.artillexstudios.axminions.minions.actions.filters.Filter;
import com.artillexstudios.axminions.utils.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Material;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MaterialFilter extends Filter<Material> {
    private final ObjectArrayList<Material> allowed = new ObjectArrayList<>(); // TODO: Check performance vs hashset

    public MaterialFilter(Map<Object, Object> configuration) {
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
                    this.allowed.removeAll(List.of(Material.values()));
                } else {
                    List<Material> materials = match(s);
                    if (materials == null) {
                        LogUtils.warn("No materials matching {} were found!", s);
                        continue;
                    }

                    this.allowed.removeAll(materials);
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
        return Collections.binarySearch(this.allowed, object) >= 0;
    }

    @Override
    public Class<?> inputClass() {
        return Material.class;
    }
}
