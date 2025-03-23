package com.artillexstudios.axminions;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.dependencies.DependencyManagerWrapper;
import com.artillexstudios.axapi.metrics.AxMetrics;
import com.artillexstudios.axapi.utils.AsyncUtils;
import com.artillexstudios.axapi.utils.PaperUtils;
import com.artillexstudios.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axminions.command.AxMinionsCommand;
import com.artillexstudios.axminions.config.Config;
import com.artillexstudios.axminions.config.Language;
import com.artillexstudios.axminions.config.Minions;
import com.artillexstudios.axminions.config.Skins;
import com.artillexstudios.axminions.database.DataHandler;
import com.artillexstudios.axminions.database.DatabaseConnector;
import com.artillexstudios.axminions.listeners.BlockPlaceListener;
import com.artillexstudios.axminions.listeners.ChunkListener;
import com.artillexstudios.axminions.listeners.MinionPlaceListener;
import com.artillexstudios.axminions.listeners.PlayerListener;
import com.artillexstudios.axminions.listeners.WorldListener;
import com.artillexstudios.axminions.minions.MinionArea;
import com.artillexstudios.axminions.minions.MinionSaver;
import com.artillexstudios.axminions.minions.MinionTicker;
import com.artillexstudios.axminions.minions.MinionWorldCache;
import com.artillexstudios.axminions.minions.ticker.BukkitMinionTicker;
import com.artillexstudios.axminions.minions.ticker.FoliaMinionTicker;
import com.artillexstudios.axminions.utils.ReloadUtils;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.concurrent.CompletableFuture;

public final class AxMinionsPlugin extends AxPlugin {
    private static AxMinionsPlugin instance;
    private DatabaseConnector connector;
    private DataHandler handler;
    private AxMetrics metrics;
    private MinionTicker ticker;
    private MinionSaver minionSaver;

    @Override
    public void updateFlags(FeatureFlags flags) {
        flags.PACKET_ENTITY_TRACKER_ENABLED.set(true);
    }

    @Override
    public void dependencies(DependencyManagerWrapper manager) {
        manager.repository("https://redempt.dev/");
        manager.dependency("com{}github{}Redempt:Crunch:2.0.3");
        manager.dependency("org{}jooq:jooq:3.19.10");
        manager.dependency("com{}zaxxer:HikariCP:5.1.0");
        manager.relocate("org{}jooq", "com.artillexstudios.axminions.jooq");
        manager.relocate("redempt{}crunch", "com.artillexstudios.axminions.crunch");
        manager.relocate("com{}zaxxer", "com.artillexstudios.axminions.hikaricp");
    }

    @Override
    public void load() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this)
                .setNamespace("axminions")
                .skipReloadDatapacks(true)
        );
    }

    @Override
    public void enable() {
        instance = this;
        if (StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(ReloadUtils::isReload)) {
            LogUtils.error("AxMinions does not support reloading! Disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        Config.reload();
        AsyncUtils.setup(Config.asyncProcessorPoolSize);
        this.metrics = new AxMetrics(this, 5);
        this.metrics.start();

        this.connector = new DatabaseConnector();
        this.handler = new DataHandler(this.connector);
        this.handler.setup().thenRun(() -> {
            if (Config.debug) {
                LogUtils.debug("Loaded database!");
            }
        });

        Language.reload();
        Skins.reload();
        Minions.reload();

        for (World world : Bukkit.getWorlds()) {
            MinionWorldCache.loadArea(world);
            MinionArea area = MinionWorldCache.getArea(world);
            for (Chunk chunk : world.getLoadedChunks()) {
                area.startTicking(chunk);
            }
        }

        CompletableFuture.allOf(Minions.loadingMinions().toArray(new CompletableFuture[0])).thenRun(() -> {
            Minions.loadingMinions().clear();

            for (World world : Bukkit.getWorlds()) {
                this.handler.loadMinions(world).toCompletableFuture().thenAccept(loaded -> {
                    if (Config.debug) {
                        LogUtils.debug("Loaded {} minions in world {} in {} ms!", loaded.firstInt(), world.getName(), loaded.secondLong() / 1_000_000);
                    }
                });
            }
        });

        Bukkit.getPluginManager().registerEvents(new WorldListener(), this);
        Bukkit.getPluginManager().registerEvents(new ChunkListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new BlockPlaceListener(), this);
        Bukkit.getPluginManager().registerEvents(new MinionPlaceListener(), this);

        this.minionSaver = new MinionSaver();
        this.minionSaver.start();

        if (PaperUtils.isFolia()) {
            this.ticker = new FoliaMinionTicker();
        } else {
            this.ticker = new BukkitMinionTicker();
        }

        this.ticker.start();
        AxMinionsCommand.register();
        CommandAPI.onEnable();
    }

    @Override
    public void disable() {
        if (this.metrics != null) {
            this.metrics.cancel();
        }

        CommandAPI.onDisable();
        this.ticker.cancel();
        this.minionSaver.stop();
        AsyncUtils.stop();
        this.connector.close();
    }

    public DataHandler handler() {
        return this.handler;
    }

    public static AxMinionsPlugin instance() {
        return instance;
    }
}
