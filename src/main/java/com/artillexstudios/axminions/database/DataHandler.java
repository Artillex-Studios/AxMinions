package com.artillexstudios.axminions.database;

import com.artillexstudios.axapi.database.DatabaseHandler;
import com.artillexstudios.axapi.database.DatabaseQuery;
import com.artillexstudios.axapi.database.handler.ListHandler;
import com.artillexstudios.axapi.database.handler.TransformerHandler;
import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.libs.snakeyaml.external.biz.base64Coder.Base64Coder;
import com.artillexstudios.axapi.nms.wrapper.ServerPlayerWrapper;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.AsyncUtils;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axminions.config.Config;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.MinionData;
import com.artillexstudios.axminions.minions.MinionType;
import com.artillexstudios.axminions.minions.MinionWorldCache;
import com.artillexstudios.axminions.minions.PlacedMinionData;
import com.artillexstudios.axminions.users.User;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.IntLongPair;
import it.unimi.dsi.fastutil.longs.LongLongPair;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class DataHandler {
    private static final int FAILED_QUERY = -3042141;
    private final DatabaseQuery<?> setupQuery;
    private final DatabaseQuery<List<PlacedMinionData>> loadQuery;
    private final DatabaseQuery<User> userSelectQuery;
    private final DatabaseQuery<Integer> userInsertQuery;
    private final DatabaseQuery<Integer> typeSelectQuery;
    private final DatabaseQuery<Integer> typeInsertQuery;
    private final DatabaseQuery<Integer> locationSelectQuery;
    private final DatabaseQuery<Integer> locationInsertQuery;
    private final DatabaseQuery<Integer> worldSelectQuery;
    private final DatabaseQuery<Integer> worldInsertQuery;
    private final DatabaseQuery<Integer> minionInsertQuery;
    private final DatabaseQuery<Integer> saveMinionsQuery;

    public DataHandler(DatabaseHandler handler) {
        this.setupQuery = handler.query("setup");
        this.loadQuery = handler.query("load", new ListHandler<>(new TransformerHandler<>(PlacedMinionData.class)));
        this.userSelectQuery = handler.query("user_select");
        this.userInsertQuery = handler.query("user_insert");
        this.typeSelectQuery = handler.query("type_select");
        this.typeInsertQuery = handler.query("type_insert");
        this.locationSelectQuery = handler.query("location_select");
        this.locationInsertQuery = handler.query("location_insert");
        this.worldSelectQuery = handler.query("world_select");
        this.worldInsertQuery = handler.query("world_insert");
        this.minionInsertQuery = handler.query("minion_insert");
        this.saveMinionsQuery = handler.query("save_minions");
    }

    public CompletableFuture<Integer> setup() {
        return this.setupQuery.createAsync(AsyncUtils.executor())
                .update()
                .exceptionallyAsync(throwable -> {
                    LogUtils.error("An unexpected error occurred while running setup query!", throwable);
                    return FAILED_QUERY;
                });
    }

    // TODO:
    //            AsyncUtils.executor().submit(() -> {
    //                this.connector.context().fetch("PRAGMA journal_mode=WAL;");
    //                this.connector.context().execute("PRAGMA synchronous = off;");
    //                this.connector.context().execute("PRAGMA page_size = 32768;");
    //                this.connector.context().fetch("PRAGMA mmap_size = 30000000000;");
    //                pragma.complete(1);
    //            });

    //
    public CompletionStage<User> loadUser(Player player) {
        String texture = ServerPlayerWrapper.wrap(player).textures().texture();
        if (Config.debug) {
            LogUtils.debug("Updating user! Texture: {}", texture == null ? "null" : texture);
        }
        return CompletableFuture.supplyAsync(() -> {
            User foundUser = this.userSelectQuery.create()
                    .query(player.getUniqueId());

            if (foundUser != null) {
                User user = foundUser.texture(texture);
                // TODO: Figure out a better way than copying
                for (Minion minion : MinionWorldCache.copy()) {
                    if (minion.ownerId() == user.id()) {
                        user.minions().add(minion);
                    }
                }

                return user;
            }

            Integer userId = this.userInsertQuery.create()
                    .execute(player.getUniqueId(), player.getName(), 0, 0);

            if (userId == null) {
                LogUtils.warn("Failed to get inserted column id for user {}!", player.getName());
                return null;
            }

            return new User(userId, player.getUniqueId(), player.getName(), texture, 0, 0, 0, new ArrayList<>());
        }, AsyncUtils.executor()).exceptionallyAsync(throwable -> {
            LogUtils.error("An unexpected error occurred while updating user {}!", player.getName(), throwable);
            return null;
        }, AsyncUtils.executor());
    }

    public short worldId(World world) {
        Integer worldId = this.worldSelectQuery.create()
                .query(world.getUID());

        if (worldId != null) {
            return worldId.shortValue();
        }

        worldId = this.worldInsertQuery.create()
                .execute(world.getUID());

        if (worldId == null) {
            return (short) FAILED_QUERY;
        }

        return worldId.shortValue();
    }

    public int locationId(int world, Location location) {
        Integer locationId = this.locationSelectQuery.create()
                .query(world, location.getBlockX(), location.getBlockY(), location.getBlockZ());

        if (locationId != null) {
            return locationId;
        }

        locationId = this.locationInsertQuery.create()
                .execute(world, location.getBlockX(), location.getBlockY(), location.getBlockZ());

        if (locationId == null) {
            return FAILED_QUERY;
        }

        return locationId;
    }

    public CompletionStage<Minion> insertMinion(Location location, MinionData data) {
        return CompletableFuture.supplyAsync(() -> {
            World world = location.getWorld();
            if (world == null) {
                return null;
            }

            short worldId = this.worldId(world);
            if (worldId == (short) FAILED_QUERY) {
                LogUtils.error("Failed worldId fetching!");
                return null;
            }

            int locationId = this.locationId(worldId, location);
            if (locationId == FAILED_QUERY) {
                LogUtils.error("Failed locationId fetching!");
                return null;
            }

            Integer id = this.minionInsertQuery.create()
                    .execute(locationId,
                            data.ownerId(),
                            data.type().id(),
                            data.level().id(),
                            data.charge(),
                            data.direction().ordinal(),
                            data.tool() == null || data.tool().getType().isAir() || !Config.requireTool ? null : WrappedItemStack.wrap(data.tool()).serialize(),
                            MinionData.serialize(data.extraData())
                    );

            return new Minion(id, location, data);
        }, AsyncUtils.executor()).exceptionallyAsync(throwable -> {
            LogUtils.error("An unexpected error occurred while inserting minion!", throwable);
            return null;
        }, AsyncUtils.executor());
    }

    public CompletionStage<IntLongPair> loadMinions(World world) {
        return CompletableFuture.supplyAsync(() -> {
            int loadedMinions = 0;
            long start = System.nanoTime();
            List<Minion> toLoad = new ArrayList<>();
            List<PlacedMinionData> query = this.loadQuery.create()
                    .query(world.getName());

            for (PlacedMinionData data : query) {
                Minion minion = data.create();
                toLoad.add(minion);
                minion.spawn();
                loadedMinions++;
            }

            CompletableFuture.runAsync(() -> MinionWorldCache.addAll(toLoad), command -> Scheduler.get().run(command)).join();
            long took = System.nanoTime() - start;
            return IntLongPair.of(loadedMinions, took);
        }, AsyncUtils.executor()).exceptionallyAsync(throwable -> {
            LogUtils.error("An unexpected error occurred while loading minions in world {}!", world.getName(), throwable);
            return IntLongPair.of(0, 0);
        }, AsyncUtils.executor());
    }

    public CompletionStage<Short> insertType(MinionType type) {
        Preconditions.checkNotNull(type, "Tried to insert null miniontype");
        return CompletableFuture.supplyAsync(() -> {
            Integer typeId = this.typeSelectQuery.create()
                    .query(type.name());

            if (typeId != null) {
                return typeId.shortValue();
            }

            typeId = this.typeInsertQuery.create()
                    .execute(type.name());

            if (typeId == null) {
                LogUtils.warn("Failed to insert type {}!", type.name());
                return (short) FAILED_QUERY;
            }

            return typeId.shortValue();
        }).exceptionallyAsync(throwable -> {
            LogUtils.error("An unexpected error occurred while inserting minion type {}!", type.name(), throwable);
            return (short) FAILED_QUERY;
        }, AsyncUtils.executor());
    }

    public CompletionStage<LongLongPair> saveMinions(Collection<Minion> minions) {
        Preconditions.checkNotNull(minions, "Tried to save null minions");
        return CompletableFuture.supplyAsync(() -> {
            long start = System.nanoTime();
            List<Object[]> queries = new ArrayList<>();
            for (Minion minion : minions) {
                if (!minion.needsSaving()) {
                    continue;
                }

                minion.save();
                queries.add(new Object[]{minion.level().id(), minion.charge(), minion.facing().ordinal(), minion.tool() == null || minion.tool().getType().isAir() || !Config.requireTool ? null : WrappedItemStack.wrap(minion.tool()).serialize(), Base64Coder.decodeLines(MinionData.serialize(minion.extraData())), minion.id()});
            }

            this.saveMinionsQuery.create()
                    .batch(queries);

//            if (Config.database.type == DatabaseType.SQLITE) {
//                this.connector.context().execute("PRAGMA wal_checkpoint(FULL);");
//            }

            long took = System.nanoTime() - start;
            return LongLongPair.of(queries.size(), took);
        }).exceptionallyAsync(throwable -> {
            LogUtils.error("An unexpected error occurred while saving minions!", throwable);
            return LongLongPair.of(FAILED_QUERY, FAILED_QUERY);
        }, AsyncUtils.executor());
    }
}
