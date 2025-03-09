package com.artillexstudios.axminions.minions.skins;

import com.artillexstudios.axapi.utils.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import java.util.Locale;

public final class SkinRegistry {
    private static final Object2ObjectArrayMap<String, Skin> SKINS = new Object2ObjectArrayMap<>();

    public static void register(Skin skin) {
        String id = skin.id().toLowerCase(Locale.ENGLISH);
        if (SKINS.containsKey(id)) {
            LogUtils.warn("Attempted to register already registered skin {}!", id);
            return;
        }

        SKINS.put(id, skin);
    }

    public static void unregister(String id) {
        String skinId = id.toLowerCase(Locale.ENGLISH);
        if (!SKINS.containsKey(skinId)) {
            LogUtils.warn("Attempted to unregister non-existent skin {}!", id);
            return;
        }

        SKINS.remove(skinId);
    }

    public static void clear() {
        SKINS.clear();
    }

    public static Skin parse(String id) {
        return SKINS.get(id.toLowerCase(Locale.ENGLISH));
    }
}
