package com.artillexstudios.axminions.minions.skins;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public final class SkinRegistry {
    private static final Logger log = LoggerFactory.getLogger(SkinRegistry.class);
    private static final Object2ObjectArrayMap<String, Skin> SKINS = new Object2ObjectArrayMap<>();

    public static void register(Skin skin) {
        String id = skin.id().toLowerCase(Locale.ENGLISH);
        if (SKINS.containsKey(id)) {
            log.warn("Attempted to register already registered skin {}!", id);
            return;
        }

        SKINS.put(id, skin);
    }

    public static void unregister(String id) {
        String skinId = id.toLowerCase(Locale.ENGLISH);
        if (!SKINS.containsKey(skinId)) {
            log.warn("Attempted to unregister non-existent skin {}!", id);
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
