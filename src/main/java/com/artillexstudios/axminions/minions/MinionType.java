package com.artillexstudios.axminions.minions;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.items.component.DataComponents;
import com.artillexstudios.axapi.items.nbt.CompoundTag;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axminions.AxMinionsPlugin;
import com.artillexstudios.axminions.api.events.MinionTypeLoadEvent;
import com.artillexstudios.axminions.exception.MinionTickFailException;
import com.artillexstudios.axminions.exception.MinionTypeNotYetLoadedException;
import com.artillexstudios.axminions.exception.MinionWarningException;
import com.artillexstudios.axminions.minions.actions.CompiledAction;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;

public final class MinionType {
    private static final int UNINITIALIZED = -412341210;
    private final ObjectArrayList<CompiledAction> actions = new ObjectArrayList<>();
    private final Int2ObjectArrayMap<Level> levels = new Int2ObjectArrayMap<>();
    private ItemStack tool;
    private final String name;
    private final AtomicInteger id = new AtomicInteger(UNINITIALIZED);
    private final Config config;

    public MinionType(String name, Config config) {
        this.name = name;
        this.config = config;
    }

    public CompletionStage<Void> load() {
        return AxMinionsPlugin.instance().handler().insertType(this).thenAccept(result -> {
            if (result == null) {
                return;
            }

            if (!this.id.compareAndSet(UNINITIALIZED, result)) {
                LogUtils.error("Abandon ship! Something went really sideways, and this miniontype ({}) has already been initialized!", this.name);
                return;
            }

            List<Map<Object, Object>> actions = this.config.getMapList("actions");
            for (Map<Object, Object> action : actions) {
                this.actions.add(CompiledAction.of(action));
            }

            int maxLevel = this.config.getInt("max-level");
            List<Map<Object, Object>> levels = this.config.getMapList("levels");
            for (Map<Object, Object> level : levels) {
                Level levelInstance = Level.of(level);
                if (levelInstance == null) {
                    continue;
                }

                this.levels.put(levelInstance.id(), levelInstance);
            }

            // Fill back up the things
            for (int i = 1; i <= maxLevel; i++) {
                Level level = this.levels.get(i);
                if (level == null) {
                    this.levels.put(i, this.levels.get(i - 1));
                }
            }

            if (!com.artillexstudios.axminions.config.Config.requireTool) {
                if (!this.config.getBackingDocument().contains("tool.default")) {
                    LogUtils.warn("Failed to load minion {}, due to default tool missing, but not requiring a tool from the user!", this.name);
                    return;
                }

                this.tool = new ItemBuilder(this.config.getSection("tool.default")).get();
            }

            MinionTypes.register(this);
            Bukkit.getPluginManager().callEvent(new MinionTypeLoadEvent(this));
        }).exceptionallyAsync(throwable -> {
            LogUtils.warn("An unexpected error occurred on miniontype registration!", throwable);
            return null;
        });
    }

    public boolean tick(Minion minion) {
        for (CompiledAction action : this.actions) {
            try {
                action.run(minion);
            } catch (MinionWarningException exception) {
                if (com.artillexstudios.axminions.config.Config.debug) {
                    LogUtils.debug("Termination of tick due to warning!");
                }
                // Rethrow the exception, so we can handle it in the tick
                throw exception;
            } catch (MinionTickFailException exception) {
                if (com.artillexstudios.axminions.config.Config.debug) {
                    LogUtils.debug("Termination of tick due to exception!");
                }

                return false;
            }
        }

        return true;
    }

    public ItemStack item(MinionData data) {
        WrappedItemStack wrappedItemStack = new ItemBuilder(this.config.getSection("item")).wrapped();
        CompoundTag tag = wrappedItemStack.get(DataComponents.customData());
        tag.putString("axminions_minion_type", this.name);
        tag.putInt("axminions_minion_level", data.level().id());
        if (com.artillexstudios.axminions.config.Config.saveStatistics) {
            tag.putString("axminions_minion_statistics", MinionData.serialize(data.extraData()));
        }
        tag.putString("axminions_minion_skin", data.skin() == null ? "" : data.skin().id());
        tag.putLong("axminions_minion_charge", data.charge());

        wrappedItemStack.set(DataComponents.customData(), tag);
        wrappedItemStack.finishEdit();
        return wrappedItemStack.toBukkit();
    }

    public ItemStack tool() {
        return this.tool;
    }

    public Level level(int level) {
        return this.levels.get(level);
    }

    public Int2ObjectArrayMap<Level> levels() {
        return levels;
    }

    public int id() {
        int id = this.id.get();
        if (id == UNINITIALIZED) {
            throw new MinionTypeNotYetLoadedException();
        }

        return id;
    }

    public String name() {
        return this.name;
    }
}
