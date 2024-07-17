package com.artillexstudios.axminions.minions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

public final class MinionTypes {
    private static final ConcurrentHashMap<String, MinionType> TYPES = new ConcurrentHashMap<>(8);
    private static final Collection<String> KEY_SET = Collections.unmodifiableCollection(TYPES.keySet());
    private static final Logger log = LoggerFactory.getLogger(MinionTypes.class);

    public static void register(MinionType minionType) {
        String lowercase = minionType.name().toLowerCase(Locale.ENGLISH);
        if (TYPES.containsKey(lowercase)) {
            log.error("A minion type with id {} is already registered!", lowercase);
            return;
        }

        TYPES.put(lowercase, minionType);
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
