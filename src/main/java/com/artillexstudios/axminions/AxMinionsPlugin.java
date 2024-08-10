package com.artillexstudios.axminions;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.libs.libby.BukkitLibraryManager;
import com.artillexstudios.axapi.utils.FeatureFlags;
import com.artillexstudios.axapi.utils.PaperUtils;
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
import com.artillexstudios.axminions.minions.MinionTicker;
import com.artillexstudios.axminions.minions.MinionWorldCache;
import com.artillexstudios.axminions.minions.ticker.BukkitMinionTicker;
import com.artillexstudios.axminions.minions.ticker.FoliaMinionTicker;
import com.artillexstudios.axminions.utils.AsyncUtils;
import com.artillexstudios.axminions.utils.LogUtils;
import com.artillexstudios.axminions.utils.ReloadUtils;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.concurrent.CompletableFuture;

public final class AxMinionsPlugin extends AxPlugin {
    private static AxMinionsPlugin instance;
    private Metrics metrics;
    private MinionTicker ticker;

    public static AxMinionsPlugin getInstance() {
        return instance;
    }

    @Override
    public void updateFlags() {
        FeatureFlags.PACKET_ENTITY_TRACKER_ENABLED.set(true);
    }

    @Override
    public void load() {
        BukkitLibraryManager libraryManager = new BukkitLibraryManager(this);
        libraryManager.configureFromJSON("libraries.json");

        CommandAPI.onLoad(new CommandAPIBukkitConfig(this)
                .setNamespace("axminions")
                .skipReloadDatapacks(true)
        );
    }

    @Override
    public void enable() {
        instance = this;
        Config.reload();
        if (StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(ReloadUtils::isReload)) {
            LogUtils.error("AxMinions does not support reloading! Disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        AsyncUtils.setup();

        if (Config.USE_BSTATS) {
            this.metrics = new Metrics(this, 19043);
        }

        DataHandler.setup().thenRun(() -> LogUtils.debug("Loaded database!"));

        this.reload();

        for (World world : Bukkit.getWorlds()) {
            MinionWorldCache.loadArea(world);
        }

        CompletableFuture.allOf(Minions.loadingMinions().toArray(new CompletableFuture[0])).thenRun(() -> {
            Minions.loadingMinions().clear();

            for (World world : Bukkit.getWorlds()) {
                DataHandler.loadMinions(world).toCompletableFuture().thenAccept(loaded -> {
                    LogUtils.debug("Loaded {} minions in world {} in {} ms!", loaded.firstInt(), world.getName(), loaded.secondLong() / 1_000_000);
                });
            }
        });

        Bukkit.getPluginManager().registerEvents(new WorldListener(), this);
        Bukkit.getPluginManager().registerEvents(new ChunkListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new BlockPlaceListener(), this);
        Bukkit.getPluginManager().registerEvents(new MinionPlaceListener(), this);

        if (PaperUtils.isFolia()) {
            ticker = new FoliaMinionTicker();
        } else {
            ticker = new BukkitMinionTicker();
        }

        ticker.start();
        AxMinionsCommand.register();
        CommandAPI.onEnable();
    }

    @Override
    public void disable() {
        if (this.metrics != null) {
            this.metrics.shutdown();
        }

        ticker.cancel();
        CommandAPI.onDisable();
        AsyncUtils.stop();
        DatabaseConnector.getInstance().close();
    }

    @Override
    public void reload() {
        Config.reload();
        Language.reload();
        Skins.reload();
        Minions.reload();
    }
}
