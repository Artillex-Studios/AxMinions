package com.artillexstudios.axminions.utils;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MaterialMatcher {

    public static List<Material> matchAll(List<String> input) {
        List<Material> materials = new ArrayList<>();
        for (String s : input) {
            if (s.equals("*")) {
                materials.addAll(List.of(Material.values()));
                return materials;
            }

            materials.addAll(match(s));
        }

        return materials;
    }

    public static List<Material> match(String material) {
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
}
