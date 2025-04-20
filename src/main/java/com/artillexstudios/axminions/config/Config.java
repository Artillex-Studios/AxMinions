package com.artillexstudios.axminions.config;

import com.artillexstudios.axapi.config.YamlConfiguration;
import com.artillexstudios.axapi.config.annotation.Comment;
import com.artillexstudios.axapi.config.annotation.ConfigurationPart;
import com.artillexstudios.axapi.config.annotation.PostProcess;
import com.artillexstudios.axapi.database.DatabaseConfig;
import com.artillexstudios.axapi.libs.snakeyaml.DumperOptions;
import com.artillexstudios.axapi.utils.YamlUtils;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axminions.AxMinionsPlugin;
import com.artillexstudios.axminions.utils.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;

public final class Config implements ConfigurationPart {
    private static final Config INSTANCE = new Config();
    public static DatabaseConfig database = new DatabaseConfig();

    @Comment("""
            Should minions require tools to work?
            """)
    public static boolean requireTool = true;
    @Comment("""
            How many blocks can the tree collector collect at once?
            """)
    public static int treeCollectorMaxCollected = 100;
    @Comment("""
            How many blocks higher should the hologram be from the location
            of the minion?
            """)
    public static float warningHologramYOffset = 1.35f;
    @Comment("""
            How often should we tick minions?
            Increasing this might improve performance a bit.
            If this is increased, minion animations might look a bit choppy.
            """)
    public static int tickFrequency = 1;
    @Comment("""
            If the minions should show a hand animation
            This has a minimal impact on performance
            """)
    public static boolean showHandAnimation = true;
    @Comment("""
            If we should process hand animations
            on a different thread from the server thread
            this has a minimal impact on performance,
            it might even be slower in some cases due to thread
            context switching.
            """)
    public static boolean asyncHandAnimation = false;
    @Comment("""
            If the minion should kill the entity instantly, and not
            deal the amount of damage the item would deal.
            """)
    public static boolean instantKill = false;
    @Comment("""
            This setting controls if the minion should retain its
            statistics after being broken. This will make minions
            with different statistics not stack, when picked up.
            """)
    public static boolean saveStatistics = true;

    @Comment("""
            How often should changes to minions get saved?
            This setting controls how often minions get saved
            into the database if they have changed.
            """)
    public static int autosaveSeconds = 300;
    @Comment("""
            The pool size of the asynchronous executor
            we use to process some things asynchronously,
            like database queries.
            """)
    public static int asyncProcessorPoolSize = 3;

    @Comment("""
            What language file should we load from the lang folder?
            You can create your own aswell! We would appreciate if you
            contributed to the plugin by creating a pull request with your translation!
            """)
    public static String language = "en_US";
    @Comment("""
            If we should send debug messages in the console
            You shouldn't enable this, unless you want to see what happens in the code
            """)
    public static boolean debug = false;
    @Comment("Do not touch!")
    public static int configVersion = 1;
    private YamlConfiguration config = null;

    public static boolean reload() {
        return INSTANCE.refreshConfig();
    }

    private boolean refreshConfig() {
        Path path = FileUtils.PLUGIN_DIRECTORY.resolve("config.yml");
        if (Files.exists(path)) {
            if (!YamlUtils.suggest(path.toFile())) {
                return false;
            }
        }

        if (this.config == null) {
            this.config = YamlConfiguration.of(path, Config.class)
                    .configVersion(1, "config-version")
                    .withDefaults(AxMinionsPlugin.instance().getResource("config.yml"))
                    .withDumperOptions(options -> {
                        options.setPrettyFlow(true);
                        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                    }).build();
        }

        this.config.load();
        return true;
    }

    @PostProcess
    public static void postProcess() {
        if (autosaveSeconds <= 0) {
            LogUtils.warn("It is not recommended to set autosave-seconds to <= 5, as this might degrade performance!");
            autosaveSeconds = 15;
        }

        if (database.pool.maximumPoolSize < 1) {
            LogUtils.warn("Maximum database pool size is lower than 1! This is not supported! Defaulting to 1.");
            database.pool.maximumPoolSize = 1;
        }
    }
}
