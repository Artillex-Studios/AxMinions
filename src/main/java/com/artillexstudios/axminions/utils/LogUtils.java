package com.artillexstudios.axminions.utils;

import com.artillexstudios.axminions.config.Config;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

public final class LogUtils {
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    public static void debug(String message) {
        if (Config.DEBUG) {
            LoggerFactory.getLogger(STACK_WALKER.getCallerClass()).info(message);
            com.artillexstudios.axapi.utils.LogUtils.log(message);
        }
    }

    public static void debug(String message, Object object) {
        if (Config.DEBUG) {
            LoggerFactory.getLogger(STACK_WALKER.getCallerClass()).info(message, object);

            String formatted = StringUtils.replace(message, "{}", object == null ? "null" : object.toString(), 1);
            com.artillexstudios.axapi.utils.LogUtils.log(formatted);
        }
    }

    public static void debug(String message, Object object, Object object2) {
        if (Config.DEBUG) {
            LoggerFactory.getLogger(STACK_WALKER.getCallerClass()).info(message, object, object2);

            String formatted = StringUtils.replace(message, "{}", object == null ? "null" : object.toString(), 1);
            formatted = StringUtils.replace(formatted, "{}", object2 == null ? "null" : object2.toString(), 1);
            com.artillexstudios.axapi.utils.LogUtils.log(formatted);
        }
    }

    public static void debug(String message, Object object, Object object2, Object object3) {
        if (Config.DEBUG) {
            LoggerFactory.getLogger(STACK_WALKER.getCallerClass()).info(message, object, object2, object3);

            String formatted = StringUtils.replace(message, "{}", object == null ? "null" : object.toString(), 1);
            formatted = StringUtils.replace(formatted, "{}", object2 == null ? "null" : object2.toString(), 1);
            formatted = StringUtils.replace(formatted, "{}", object3 == null ? "null" : object3.toString(), 1);
            com.artillexstudios.axapi.utils.LogUtils.log(formatted);
        }
    }

    public static void debug(String message, Object object, Object object2, Object object3, Object object4) {
        if (Config.DEBUG) {
            LoggerFactory.getLogger(STACK_WALKER.getCallerClass()).info(message, object, object2, object3, object4);

            String formatted = StringUtils.replace(message, "{}", object == null ? "null" : object.toString(), 1);
            formatted = StringUtils.replace(formatted, "{}", object2 == null ? "null" : object2.toString(), 1);
            formatted = StringUtils.replace(formatted, "{}", object3 == null ? "null" : object3.toString(), 1);
            formatted = StringUtils.replace(formatted, "{}", object4 == null ? "null" : object4.toString(), 1);
            com.artillexstudios.axapi.utils.LogUtils.log(formatted);
        }
    }

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

    public static void error(String message, Object... arguments) {
        LoggerFactory.getLogger(STACK_WALKER.getCallerClass()).error(message, arguments);
    }

    public static void info(String message, Object... arguments) {
        LoggerFactory.getLogger(STACK_WALKER.getCallerClass()).info(message, arguments);
    }
}
