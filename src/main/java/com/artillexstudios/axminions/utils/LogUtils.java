package com.artillexstudios.axminions.utils;

import com.artillexstudios.axminions.config.Config;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

public final class LogUtils {
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    public static void debug(String message, Object... arguments) {
        if (Config.DEBUG) {
            LoggerFactory.getLogger(STACK_WALKER.getCallerClass()).info(message, arguments);

            String formatted = message;
            for (Object argument : arguments) {
                formatted = StringUtils.replace(formatted, "{}", argument == null ? "null" : argument.toString(), 1);
            }
            com.artillexstudios.axapi.utils.LogUtils.log(formatted);
        }
    }

    public static void warn(String message, Object... arguments) {
        LoggerFactory.getLogger(STACK_WALKER.getCallerClass()).warn(message, arguments);
    }

    public static void info(String message, Object... arguments) {
        LoggerFactory.getLogger(STACK_WALKER.getCallerClass()).info(message, arguments);
    }
}
