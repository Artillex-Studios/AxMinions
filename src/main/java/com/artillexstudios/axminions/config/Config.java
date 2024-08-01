package com.artillexstudios.axminions.config;

import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.dvs.versioning.BasicVersioning;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.dumper.DumperSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.general.GeneralSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.loader.LoaderSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.updater.UpdaterSettings;
import com.artillexstudios.axapi.utils.YamlUtils;
import com.artillexstudios.axminions.AxMinionsPlugin;
import com.artillexstudios.axminions.database.DatabaseType;
import com.artillexstudios.axminions.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public final class Config {
    private static final Logger log = LoggerFactory.getLogger(Config.class);
    public static String DATABASE_ADDRESS = "127.0.0.1";
    public static int DATABASE_PORT = 3306;
    public static String DATABASE_DATABASE = "admin";
    public static String DATABASE_USERNAME = "admin";
    public static String DATABASE_PASSWORD = "admin";
    public static int DATABASE_MAXIMUM_POOL_SIZE = 10;
    public static int DATABASE_MINIMUM_IDLE = 10;
    public static int DATABASE_MAXIMUM_LIFETIME = 1800000;
    public static int DATABASE_KEEPALIVE_TIME = 0;
    public static int DATABASE_CONNECTION_TIMEOUT = 5000;
    public static DatabaseType DATABASE_TYPE = DatabaseType.H2;
    public static int TICK_FREQUENCY = 1;
    public static boolean SHOW_HAND_ANIMATION = true;
    public static boolean ASYNC_HAND_ANIMATION = false;
    public static int ASYNC_PROCESSOR_POOL_SIZE = 3;
    public static boolean REQUIRE_TOOL = true;
    public static boolean INSTANT_KILL = false;
    public static String LANGUAGE = "en_US";
    public static boolean USE_BSTATS = true;
    public static boolean DEBUG = false;
    private static final Config INSTANCE = new Config();
    private com.artillexstudios.axapi.config.Config config = null;

    public static boolean reload() {
        return INSTANCE.refreshConfig();
    }

    private boolean refreshConfig() {
        File file = FileUtils.PLUGIN_DIRECTORY.resolve("config.yml").toFile();
        if (file.exists()) {
            if (!YamlUtils.suggest(file)) {
                return false;
            }
        }

        if (config != null) {
            config.reload();
        } else {
            config = new com.artillexstudios.axapi.config.Config(file, AxMinionsPlugin.getInstance().getResource("config.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build());
        }

        refreshValues();
        return true;
    }

    private void refreshValues() {
        if (config == null) {
            log.error("Config was not loaded correctly! Using default values!");
            return;
        }

        DATABASE_TYPE = DatabaseType.parse(config.getString("database.type", DATABASE_TYPE.name()));
        DATABASE_ADDRESS = config.getString("database.address", DATABASE_ADDRESS);
        DATABASE_PORT = config.getInt("database.port", DATABASE_PORT);
        DATABASE_DATABASE = config.getString("database.database", DATABASE_DATABASE);
        DATABASE_USERNAME = config.getString("database.username", DATABASE_USERNAME);
        DATABASE_PASSWORD = config.getString("database.password", DATABASE_PASSWORD);
        DATABASE_MAXIMUM_POOL_SIZE = config.getInt("database.pool.maximum-pool-size", DATABASE_MAXIMUM_POOL_SIZE);
        DATABASE_MINIMUM_IDLE = config.getInt("database.pool.minimum-idle", DATABASE_MINIMUM_IDLE);
        DATABASE_MAXIMUM_LIFETIME = config.getInt("database.pool.maximum-lifetime", DATABASE_MAXIMUM_LIFETIME);
        DATABASE_KEEPALIVE_TIME = config.getInt("database.pool.keepalive-time", DATABASE_KEEPALIVE_TIME);
        DATABASE_CONNECTION_TIMEOUT = config.getInt("database.pool.connection-timeout", DATABASE_CONNECTION_TIMEOUT);
        TICK_FREQUENCY = config.getInt("tick-frequency", TICK_FREQUENCY);
        SHOW_HAND_ANIMATION = config.getBoolean("show-hand-animation", SHOW_HAND_ANIMATION);
        ASYNC_HAND_ANIMATION = config.getBoolean("async-hand-animation", ASYNC_HAND_ANIMATION);
        ASYNC_PROCESSOR_POOL_SIZE = config.getInt("async-processor-pool-size", ASYNC_PROCESSOR_POOL_SIZE);
        REQUIRE_TOOL = config.getBoolean("require-tool", REQUIRE_TOOL);
        INSTANT_KILL = config.getBoolean("instant-kill", INSTANT_KILL);
        LANGUAGE = config.getString("language", LANGUAGE);
        USE_BSTATS = config.getBoolean("use-bstats", USE_BSTATS);
        DEBUG = config.getBoolean("debug", DEBUG);
    }
}
