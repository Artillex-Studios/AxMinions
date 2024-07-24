package com.artillexstudios.axminions.minions;

import com.artillexstudios.axminions.utils.LogUtils;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

public final class MinionTypes {
    private static final ConcurrentHashMap<String, MinionType> TYPES = new ConcurrentHashMap<>(8);
    private static final Short2ObjectOpenHashMap<MinionType> TYPE_IDS = new Short2ObjectOpenHashMap<>(8);
    private static final Collection<String> KEY_SET = Collections.unmodifiableCollection(TYPES.keySet());
    private static final Logger log = LoggerFactory.getLogger(MinionTypes.class);

    public static void register(MinionType minionType) {
        String lowercase = minionType.name().toLowerCase(Locale.ENGLISH);
        if (TYPES.containsKey(lowercase)) {
            log.error("A minion type with id {} is already registered!", lowercase);
            return;
        }

        TYPES.put(lowercase, minionType);
        TYPE_IDS.put((short) minionType.id(), minionType);
        LogUtils.debug("Put {} to {}", minionType.name(), (short) minionType.id());
    }

    public static MinionType parse(short id) {
        return TYPE_IDS.get(id);
    }

    public static MinionType parse(String minionType) {
        return TYPES.get(minionType.toLowerCase(Locale.ENGLISH));
    }

    public static void unregister(String minionType) {
        String lowercase = minionType.toLowerCase(Locale.ENGLISH);
        if (!TYPES.containsKey(lowercase)) {
            log.warn("Attempted to unregister non-existent skin {}!", lowercase);
            return;
        }

        TYPES.remove(lowercase);
    }

    public static Collection<String> types() {
        return KEY_SET;
    }
}
