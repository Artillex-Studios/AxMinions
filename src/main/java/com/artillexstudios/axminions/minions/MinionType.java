package com.artillexstudios.axminions.minions;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.items.component.DataComponents;
import com.artillexstudios.axapi.items.nbt.CompoundTag;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axminions.api.events.MinionTypeLoadEvent;
import com.artillexstudios.axminions.database.DataHandler;
import com.artillexstudios.axminions.exception.MinionTickFailException;
import com.artillexstudios.axminions.exception.MinionTypeNotYetLoadedException;
import com.artillexstudios.axminions.minions.actions.CompiledAction;
import com.artillexstudios.axminions.utils.FieldAccessors;
import com.artillexstudios.axminions.utils.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;

public final class MinionType {
    private static final int UNINITIALIZED = -412341210;
    private static final Logger log = LoggerFactory.getLogger(MinionType.class);
    private final ObjectArrayList<CompiledAction> actions = new ObjectArrayList<>();
    private final Int2ObjectArrayMap<Level> levels = new Int2ObjectArrayMap<>();
    private final String name;
    private final AtomicInteger id = new AtomicInteger(UNINITIALIZED);
    private final Config config;

    public MinionType(String name, Config config) {
        this.name = name;
        this.config = config;
    }

    public CompletionStage<Void> load() {
        return DataHandler.insertType(this).thenAccept(result -> {
            if (result == null) {
                return;
            }

            if (!this.id.compareAndSet(UNINITIALIZED, result)) {
                log.error("Abandon ship! Something went really sideways, and this miniontype ({}) has already been initialized!", this.name);
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
            } catch (MinionTickFailException exception) {
                LogUtils.debug("Termination of tick due to exception!");
                return false;
            }
        }

        return true;
    }

    public ItemStack item(MinionData data) {
        ItemBuilder builder = new ItemBuilder(this.config.getSection("item"));
        WrappedItemStack wrappedItemStack = FieldAccessors.STACK_ACCESSOR.get(builder);
        CompoundTag tag = wrappedItemStack.get(DataComponents.customData());
        tag.putString("axminions_minion_type", this.name);
        tag.putInt("axminions_minion_level", data.level().id());
        if (com.artillexstudios.axminions.config.Config.SAVE_STATISTICS) {
            tag.putString("axminions_minion_statistics", MinionData.serialize(data.extraData()));
        }
        tag.putString("axminions_minion_skin", data.skin() == null ? "" : data.skin().id());
        tag.putLong("axminions_minion_charge", data.charge());

        wrappedItemStack.set(DataComponents.customData(), tag);
        wrappedItemStack.finishEdit();
        return wrappedItemStack.toBukkit();
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
