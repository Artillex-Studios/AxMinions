package com.artillexstudios.axminions.utils;

import java.util.HashMap;

public final class CollectionUtils {

    public static <T, Z> HashMap<T, Z> hashMap(int size) {
        return new HashMap<>((int) Math.ceil(size / 0.75f));
    }
}
