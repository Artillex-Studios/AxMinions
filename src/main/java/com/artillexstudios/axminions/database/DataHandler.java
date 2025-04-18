package com.artillexstudios.axminions.database;

import com.artillexstudios.axapi.database.DatabaseHandler;
import com.artillexstudios.axapi.database.DatabaseQuery;
import com.artillexstudios.axapi.database.handler.ListHandler;
import com.artillexstudios.axapi.database.handler.TransformerHandler;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.AsyncUtils;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.MinionWorldCache;
import com.artillexstudios.axminions.minions.PlacedMinionData;
import it.unimi.dsi.fastutil.ints.IntLongPair;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class DataHandler {
    private static final int FAILED_QUERY = -3042141;
    private final DatabaseHandler handler;
    private final DatabaseQuery<?> setupQuery;
    private final DatabaseQuery<List<PlacedMinionData>> loadQuery;

    public DataHandler(DatabaseHandler handler) {
        this.handler = handler;
        // TODO: Implement
//        handler.addTransformer(PlacedMinionData.class, data -> {
//            return List.of(data.ownerId(), (short) data.type().id(), (short) data.direction().ordinal(), data.location().getBlockX(), data.location().getBlockY(), data.location().getBlockZ());
//        }, data -> {
//            return new PlacedMinionData(data.getFirst(), data.get(1), data.get(2), data.get(3), data.get(4), data.get(5), data.get(6), data.get(7), data.get(8), data.get(9));
//        });
        this.setupQuery = handler.query("setup");
        this.loadQuery = handler.query("load", new ListHandler<>(new TransformerHandler<>(handler, PlacedMinionData.class)));
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
//    public CompletionStage<User> loadUser(Player player) {
//        String texture = ServerPlayerWrapper.wrap(player).textures().texture();
//        if (Config.debug) {
//            LogUtils.debug("Updating user! Texture: {}", texture == null ? "null" : texture);
//        }
//        return CompletableFuture.supplyAsync(() -> {
//            Result<Record> select = this.connector.context()
//                    .select()
//                    .from(Tables.USERS)
//                    .where(Fields.UUID.eq(player.getUniqueId()))
//                    .limit(1)
//                    .fetch();
//
//            if (!select.isEmpty()) {
//                Record record = select.getFirst();
//                if (Config.debug) {
//                    LogUtils.debug("User data select record: {}", record);
//                }
//                int ownerId = record.get(Fields.ID);
//
//                Result<Record1<Integer>> minionSelect = this.connector.context()
//                        .selectCount()
//                        .from(Tables.MINIONS)
//                        .where(Fields.OWNER_ID.eq(ownerId))
//                        .limit(1)
//                        .fetch();
//
//                if (!minionSelect.isEmpty()) {
//                    Record minionRecord = minionSelect.getFirst();
//                    int minionCount = minionRecord.get(0, int.class);
//
//                    return new User(ownerId, player.getUniqueId(), player.getName(), texture, minionCount, record.get(Fields.EXTRA_SLOTS, int.class), record.get(Fields.ISLAND_SLOTS, int.class), new ArrayList<>());
//                }
//
//                return new User(ownerId, player.getUniqueId(), player.getName(), texture, 0, record.get(Fields.EXTRA_SLOTS, int.class), record.get(Fields.ISLAND_SLOTS, int.class), new ArrayList<>());
//            }
//
//            Record1<Integer> insert = this.connector.context()
//                    .insertInto(Tables.USERS)
//                    .set(Fields.UUID, player.getUniqueId())
//                    .set(Fields.NAME, player.getName())
//                    .set(Fields.EXTRA_SLOTS, 0)
//                    .set(Fields.ISLAND_SLOTS, 0)
//                    .returningResult(Fields.ID)
//                    .fetchOne();
//
//            if (insert == null) {
//                return null;
//            }
//
//            return new User(insert.get(Fields.ID), player.getUniqueId(), player.getName(), texture, 0, 0, 0, new ArrayList<>());
//        }, AsyncUtils.executor()).exceptionallyAsync(throwable -> {
//            LogUtils.error("An unexpected error occurred while updating user {}!", player.getName(), throwable);
//            return null;
//        }, AsyncUtils.executor());
//    }
//
//    public short worldId(World world) {
//        Result<Record> select = this.connector.context()
//                .select()
//                .from(Tables.WORLDS)
//                .where(Fields.WORLD_UUID.eq(world.getUID()))
//                .limit(1)
//                .fetch();
//
//        if (!select.isEmpty()) {
//            Record record = select.getFirst();
//            if (Config.debug) {
//                LogUtils.debug("World select record: {}", record);
//            }
//            return record.get(Fields.ID, short.class);
//        }
//
//        Record1<Integer> insert = this.connector.context()
//                .insertInto(Tables.WORLDS)
//                .set(Fields.WORLD_UUID, world.getUID())
//                .returningResult(Fields.ID)
//                .fetchOne();
//
//        if (insert == null) {
//            return (short) FAILED_QUERY;
//        }
//
//        return insert.get(Fields.ID, short.class);
//    }
//
//    public int locationId(int world, Location location) {
//        Result<Record> select = this.connector.context()
//                .select()
//                .from(Tables.LOCATIONS)
//                .where(Fields.WORLD_ID.eq(world)
//                        .and(Fields.LOCATION_X.eq(location.getBlockX()))
//                        .and(Fields.LOCATION_Y.eq(location.getBlockY()))
//                        .and(Fields.LOCATION_Z.eq(location.getBlockZ())))
//                .limit(1)
//                .fetch();
//
//        if (!select.isEmpty()) {
//            Record record = select.getFirst();
//            if (Config.debug) {
//                LogUtils.debug("Location select record: {}", record);
//            }
//            return record.get(Fields.ID);
//        }
//
//        Record1<Integer> insert = this.connector.context()
//                .insertInto(Tables.LOCATIONS)
//                .set(Fields.WORLD_ID, world)
//                .set(Fields.LOCATION_X, location.getBlockX())
//                .set(Fields.LOCATION_Y, location.getBlockY())
//                .set(Fields.LOCATION_Z, location.getBlockZ())
//                .returningResult(Fields.ID)
//                .fetchOne();
//
//        if (insert == null) {
//            return FAILED_QUERY;
//        }
//
//        return insert.get(Fields.ID);
//    }
//
//    public String minionType(short id) {
//        return this.connector.context()
//                .select()
//                .from(Tables.TYPES)
//                .where(Fields.ID.eq((int) id))
//                .limit(1)
//                .fetchSingle(Fields.NAME);
//    }
//
//    public CompletionStage<Void> insertMinion(Minion minion) {
//        return CompletableFuture.runAsync(() -> {
//            World world = minion.location().getWorld();
//            if (world == null) {
//                return;
//            }
//
//            short worldId = this.worldId(world);
//            if (worldId == (short) FAILED_QUERY) {
//                LogUtils.error("Failed worldId fetching!");
//                return;
//            }
//
//            int locationId = this.locationId(worldId, minion.location());
//            if (locationId == FAILED_QUERY) {
//                LogUtils.error("Failed locationId fetching!");
//                return;
//            }
//
//            this.connector.context()
//                    .insertInto(Tables.MINIONS)
//                    .set(Fields.LOCATION_ID, locationId)
//                    .set(Fields.OWNER_ID, minion.ownerId())
//                    .set(Fields.TYPE_ID, minion.type().id())
//                    .set(Fields.LEVEL, minion.level().id())
//                    .set(Fields.CHARGE, minion.charge())
//                    .set(Fields.FACING, minion.facing().ordinal())
//                    .set(Fields.TOOL, minion.tool() == null || minion.tool().getType().isAir() || !Config.requireTool ? null : WrappedItemStack.wrap(minion.tool()).serialize())
//                    .set(Fields.EXTRA_DATA, MinionData.serialize(minion.extraData()))
//                    .execute();
//        }, AsyncUtils.executor()).exceptionallyAsync(throwable -> {
//            LogUtils.error("An unexpected error occurred while inserting minion!", throwable);
//            return null;
//        }, AsyncUtils.executor());
//    }
//
    public CompletionStage<IntLongPair> loadMinions(World world) {
        return CompletableFuture.supplyAsync(() -> {
            int loadedMinions = 0;
            long start = System.nanoTime();
            List<Minion> toLoad = new ArrayList<>();
            List<PlacedMinionData> query = this.loadQuery.create().query(world.getName());
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
//
//    public CompletionStage<Short> insertType(MinionType type) {
//        Preconditions.checkNotNull(type, "Tried to insert null miniontype");
//        return CompletableFuture.supplyAsync(() -> {
//            Result<Record> select = this.connector.context()
//                    .select()
//                    .from(Tables.TYPES.getName().toLowerCase(Locale.ENGLISH))
//                    .where(Fields.NAME.eq(type.name()))
//                    .limit(1)
//                    .fetch();
//
//            if (!select.isEmpty()) {
//                Record record = select.getFirst();
//                if (Config.debug) {
//                    LogUtils.debug("select record: {}", record);
//                }
//                return record.get(Fields.ID, short.class);
//            }
//
//            Record1<Integer> insert = this.connector.context()
//                    .insertInto(Tables.TYPES)
//                    .set(Fields.NAME, type.name())
//                    .returningResult(Fields.ID)
//                    .fetchOne();
//
//            if (insert == null) {
//                return (short) FAILED_QUERY;
//            }
//
//            return insert.get(Fields.ID, short.class);
//        }).exceptionallyAsync(throwable -> {
//            LogUtils.error("An unexpected error occurred while inserting minion type {}!", type.name(), throwable);
//            return (short) FAILED_QUERY;
//        }, AsyncUtils.executor());
//    }
//
//    public CompletionStage<LongLongPair> saveMinions(Collection<Minion> minions) {
//        Preconditions.checkNotNull(minions, "Tried to save null minions");
//        return CompletableFuture.supplyAsync(() -> {
//            long start = System.nanoTime();
//            List<Query> queries = new ArrayList<>();
//            for (Minion minion : minions) {
//                if (!minion.needsSaving()) {
//                    continue;
//                }
//
//                Location location = minion.location();
//                World world = location.getWorld();
//                if (world == null) {
//                    continue;
//                }
//
//                int locationId;
//                try {
//                    int worldId = worldId(world);
//                    locationId = locationId(worldId, location);
//                } catch (Exception exception) {
//                    LogUtils.warn("An exception occurred while saving minion {}", minion, exception);
//                    continue;
//                }
//
//                Query query = this.connector.context()
//                        .update(Tables.MINIONS)
//                        .set(Fields.LEVEL, minion.level().id())
//                        .set(Fields.CHARGE, minion.charge())
//                        .set(Fields.FACING, minion.facing().ordinal())
//                        .set(Fields.TOOL, minion.tool().getType().isAir() ? null : WrappedItemStack.wrap(minion.tool()).serialize())
//                        .set(Fields.EXTRA_DATA, MinionData.serialize(minion.extraData()))
//                        .where(Fields.LOCATION_ID.eq(locationId));
//
//                minion.save();
//                queries.add(query);
//            }
//
//            this.connector.context()
//                    .batch(queries)
//                    .execute();
//
//            if (Config.database.type == DatabaseType.SQLITE) {
//                this.connector.context().execute("PRAGMA wal_checkpoint(FULL);");
//            }
//
//            long took = System.nanoTime() - start;
//            return LongLongPair.of(queries.size(), took);
//        }).exceptionallyAsync(throwable -> {
//            LogUtils.error("An unexpected error occurred while saving minions!", throwable);
//            return LongLongPair.of(FAILED_QUERY, FAILED_QUERY);
//        }, AsyncUtils.executor());
//    }
}
