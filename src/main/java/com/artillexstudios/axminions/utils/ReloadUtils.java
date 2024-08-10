package com.artillexstudios.axminions.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

public final class ReloadUtils {

    public static boolean isReload(Stream<StackWalker.StackFrame> element) {
        return element.anyMatch(it -> StringUtils.containsIgnoreCase(it.getClassName(), "reload") || StringUtils.containsIgnoreCase(it.getClassName(), "plugman"));
    }
}
