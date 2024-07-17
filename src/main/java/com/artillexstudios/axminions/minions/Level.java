package com.artillexstudios.axminions.minions;

import com.artillexstudios.axminions.minions.skins.Skin;
import com.artillexstudios.axminions.minions.skins.SkinRegistry;
import com.artillexstudios.axminions.utils.LogUtils;

import java.util.Map;

public record Level(int id, int actionTicks, Skin skin) {

    public static Level of(Map<Object, Object> map) {
        if (map == null) {
            return null;
        }

        Integer level = (Integer) map.get("level");
        if (level == null) {
            LogUtils.warn("Could not find level int in level configuration!");
            return null;
        }

        Integer actionTicks = (Integer) map.get("action-ticks");
        if (actionTicks == null) {
            LogUtils.warn("Could not find action-ticks in level configuration!");
            return null;
        }

        String skinName = (String) map.get("skin");
        if (skinName == null) {
            LogUtils.warn("Could not find skin in level configuration!");
            return null;
        }

        Skin skin = SkinRegistry.parse(skinName);
        if (skin == null) {
            LogUtils.warn("No skin found with id {}", skinName);
            return null;
        }

        return new Level(level, actionTicks, skin);
    }
}
