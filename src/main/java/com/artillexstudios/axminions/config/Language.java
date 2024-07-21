package com.artillexstudios.axminions.config;

import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.dvs.versioning.BasicVersioning;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.dumper.DumperSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.general.GeneralSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.loader.LoaderSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.updater.UpdaterSettings;
import com.artillexstudios.axapi.utils.YamlUtils;
import com.artillexstudios.axminions.AxMinionsPlugin;
import com.artillexstudios.axminions.utils.FileUtils;
import com.artillexstudios.axminions.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

public final class Language {
    private static final Logger log = LoggerFactory.getLogger(Language.class);
    private static final Path LANGUAGE_DIRECTORY = FileUtils.PLUGIN_DIRECTORY.resolve("language");
    private static final Language INSTANCE = new Language();
    public static String PREFIX = "";
    public static String RELOAD_SUCCESS = "<#00FF00>Successfully reloaded the configurations of the plugin in <white><time></white>ms!";
    public static String RELOAD_FAIL = "<#FF0000>There were some issues while reloading file(s): <white><files></white>! Please check out the console for more information! <br>Reload done in: <white><time></white>ms!";
    public static String ERROR_TYPE_NOT_FOUND = "<#FF0000>No minion type could be found with name <name>!";
    public static String ERROR_INVALID_NUMBER = "<#FF0000>The number you have provided is invalid! (<number>)";
    public static String ERROR_INVALID_LEVEL = "<#FF0000>The level provided does not exist for this minion! (<level>)";
    private com.artillexstudios.axapi.config.Config config = null;
    public static String lastLanguage;

    public static boolean reload() {
        LogUtils.debug("Reload called on language!");
        FileUtils.copyFromResource("language");

        return INSTANCE.refreshConfig();
    }

    private boolean refreshConfig() {
        LogUtils.debug("Refreshing language");
        File file = LANGUAGE_DIRECTORY.resolve(Config.LANGUAGE + ".yml").toFile();
        boolean shouldDefault = false;
        if (file.exists()) {
            LogUtils.debug("File exists");
            if (!YamlUtils.suggest(file)) {
                return false;
            }
        } else {
            shouldDefault = true;
            file = LANGUAGE_DIRECTORY.resolve("en_US.yml").toFile();
            log.error("No language configuration was found with the name {}! Defaulting to en_US...", Config.LANGUAGE);
        }

        // The user might have changed the config
        if (config != null && lastLanguage != null && lastLanguage.equalsIgnoreCase(Config.LANGUAGE)) {
            LogUtils.debug("Config not null");
            config.reload();
        } else {
            lastLanguage = shouldDefault ? "en_US" : Config.LANGUAGE;
            LogUtils.debug("Set lastLanguage to {}", lastLanguage);
            InputStream defaults = AxMinionsPlugin.getInstance().getResource("language/" + lastLanguage + ".yml");
            if (defaults == null) {
                LogUtils.debug("Defaults are null, defaulting to en_US.yml");
                defaults = AxMinionsPlugin.getInstance().getResource("language/en_US.yml");
            }

            LogUtils.debug("Loading config from file {} with defaults {}", file, defaults);
            config = new com.artillexstudios.axapi.config.Config(file, defaults, GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build());
        }

        refreshValues();
        return true;
    }

    private void refreshValues() {
        if (config == null) {
            log.error("Language configuration was not loaded correctly! Using default values!");
            return;
        }

        PREFIX = config.getString("prefix", PREFIX);
        ERROR_TYPE_NOT_FOUND = config.getString("error.type-not-found", ERROR_TYPE_NOT_FOUND);
        ERROR_INVALID_NUMBER = config.getString("error.invalid-number", ERROR_INVALID_NUMBER);
    }
}
