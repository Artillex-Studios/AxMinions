package com.artillexstudios.axminions.database;

import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.nms.NMSHandlers;
import com.artillexstudios.axminions.config.Config;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.MinionData;
import com.artillexstudios.axminions.minions.MinionType;
import com.artillexstudios.axminions.utils.LogUtils;
import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class DataHandler {
    private static final ExecutorService databaseExecutor = Executors.newFixedThreadPool(3, new ThreadFactory() {
        private static final AtomicInteger counter = new AtomicInteger(1);

        @Override
        public Thread newThread(@NotNull Runnable runnable) {
            return new Thread(null, runnable, "AxMinions-Database-Thread-" + counter.getAndIncrement());
        }
    });
    private static final int FAILED_QUERY = -3042141;
    private static final Logger log = LoggerFactory.getLogger(DataHandler.class);

    public static CompletionStage<Void> setup() {
        ArrayList<CompletableFuture<Integer>> futures = new ArrayList<>();

        CompletionStage<Integer> types = DatabaseConnector.getInstance().context().createTableIfNotExists(Tables.TYPES)
                .column(Fields.ID, SQLDataType.SMALLINT.identity(true))
                .column(Fields.NAME, SQLDataType.VARCHAR(64))
                .primaryKey(Fields.ID)
                .executeAsync(databaseExecutor)
                .exceptionallyAsync(throwable -> {
                    log.error("An unexpected error occurred while running type table creation query!", throwable);
                    return FAILED_QUERY;
                });

        futures.add(types.toCompletableFuture());
        CompletionStage<Integer> users = DatabaseConnector.getInstance().context().createTableIfNotExists(Tables.USERS)
                .column(Fields.ID, SQLDataType.INTEGER.identity(true))
                .column(Fields.UUID, SQLDataType.UUID)
                .column(Fields.NAME, SQLDataType.VARCHAR(16))
                .column(Fields.TEXTURE, SQLDataType.VARCHAR(512))
                .column(Fields.EXTRA_SLOTS, SQLDataType.INTEGER.default_(0))
                .column(Fields.ISLAND_SLOTS, SQLDataType.INTEGER.default_(0))
                .primaryKey(Fields.ID)
                .executeAsync(databaseExecutor)
                .exceptionallyAsync(throwable -> {
                    log.error("An unexpected error occurred while running user table creation query!", throwable);
                    return FAILED_QUERY;
                });

        futures.add(users.toCompletableFuture());

        CompletionStage<Integer> worlds = DatabaseConnector.getInstance().context().createTableIfNotExists(Tables.WORLDS)
                .column(Fields.ID, SQLDataType.SMALLINT.identity(true))
                .column(Fields.WORLD_UUID, SQLDataType.UUID)
                .primaryKey(Fields.ID)
                .executeAsync(databaseExecutor)
                .exceptionallyAsync(throwable -> {
                    log.error("An unexpected error occurred while running world table creation query!", throwable);
                    return FAILED_QUERY;
                });

        futures.add(worlds.toCompletableFuture());

        CompletionStage<Integer> locations = DatabaseConnector.getInstance().context().createTableIfNotExists(Tables.LOCATIONS)
                .column(Fields.ID, SQLDataType.INTEGER.identity(true))
                .column(Fields.WORLD_ID, SQLDataType.SMALLINT)
                .column(Fields.LOCATION_X, SQLDataType.INTEGER)
                .column(Fields.LOCATION_Y, SQLDataType.INTEGER)
                .column(Fields.LOCATION_Z, SQLDataType.INTEGER)
                .primaryKey(Fields.ID)
                .executeAsync(databaseExecutor)
                .exceptionallyAsync(throwable -> {
                    log.error("An unexpected error occurred while running locations table creation query!", throwable);
                    return FAILED_QUERY;
                });

        futures.add(locations.toCompletableFuture());

        CompletionStage<Integer> minions = DatabaseConnector.getInstance().context().createTableIfNotExists(Tables.MINIONS)
                .column(Fields.ID, SQLDataType.INTEGER.identity(true))
                .column(Fields.LOCATION_ID, SQLDataType.INTEGER)
                .column(Fields.OWNER_ID, SQLDataType.INTEGER)
                .column(Fields.TYPE_ID, SQLDataType.SMALLINT)
                .column(Fields.LEVEL, SQLDataType.SMALLINT)
                .column(Fields.CHARGE, SQLDataType.BIGINT)
                .column(Fields.FACING, SQLDataType.SMALLINT)
                .column(Fields.TOOL, SQLDataType.CLOB)
                .column(Fields.EXTRA_DATA, SQLDataType.CLOB)
                .primaryKey(Fields.ID)
                .executeAsync(databaseExecutor)
                .exceptionallyAsync(throwable -> {
                    log.error("An unexpected error occurred while running minions table creation query!", throwable);
                    return FAILED_QUERY;
                });

        futures.add(minions.toCompletableFuture());
        if (Config.DATABASE_TYPE == DatabaseType.SQLITE) {
            CompletableFuture<Integer> pragma = new CompletableFuture<>();
            databaseExecutor.submit(() -> {
                DatabaseConnector.getInstance().context().fetch("PRAGMA journal_mode=WAL;");
                DatabaseConnector.getInstance().context().execute("PRAGMA synchronous = off;");
                DatabaseConnector.getInstance().context().execute("PRAGMA page_size = 32768;");
                DatabaseConnector.getInstance().context().fetch("PRAGMA mmap_size = 30000000000;");
                pragma.complete(1);
            });
            futures.add(pragma);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    public static CompletionStage<Result<Record1<Integer>>> updateUser(Player player) {
        String texture = NMSHandlers.getNmsHandler().textures(player).getFirst();
        LogUtils.debug("Updating user! Texture: {}", texture == null ? "null" : texture);
        return DatabaseConnector.getInstance().context().insertInto(Tables.USERS, Fields.UUID, Fields.NAME, Fields.TEXTURE, Fields.EXTRA_SLOTS, Fields.ISLAND_SLOTS)
                .onDuplicateKeyUpdate()
                .set(Fields.UUID, player.getUniqueId())
                .set(Fields.NAME, player.getName())
                .set(Fields.TEXTURE, texture)
                .returningResult(Fields.ID)
                .fetchAsync(databaseExecutor)
                .exceptionallyAsync(throwable -> {
                    log.error("An unexpected error occurred while updating user {}!", player.getName(), throwable);
                    return null;
                });
    }

    public static int worldId(World world) {
        Result<Record> select = DatabaseConnector.getInstance().context()
                .select()
                .from(Tables.WORLDS)
                .where(Fields.WORLD_UUID.eq(world.getUID()))
                .fetch();

        if (!select.isEmpty()) {
            Record record = select.get(0);
            LogUtils.debug("World select record: {}", record);
            return (Integer) record.get("ID");
        }

        Record1<Integer> insert = DatabaseConnector.getInstance().context()
                .insertInto(Tables.WORLDS)
                .set(Fields.WORLD_UUID, world.getUID())
                .returningResult(Fields.ID)
                .fetchOne();

        if (insert == null) {
            return FAILED_QUERY;
        }

        return insert.get(Fields.ID);
    }

    public static int locationId(int world, Location location) {
        Result<Record> select = DatabaseConnector.getInstance().context()
                .select()
                .from(Tables.LOCATIONS)
                .where(Fields.WORLD_ID.eq(world)
                        .and(Fields.LOCATION_X.eq(location.getBlockX()))
                        .and(Fields.LOCATION_Y.eq(location.getBlockY()))
                        .and(Fields.LOCATION_Z.eq(location.getBlockZ())))
                .fetch();

        if (!select.isEmpty()) {
            Record record = select.get(0);
            LogUtils.debug("Location select record: {}", record);
            return (Integer) record.get("ID");
        }

        Record1<Integer> insert = DatabaseConnector.getInstance().context()
                .insertInto(Tables.LOCATIONS)
                .set(Fields.WORLD_ID, world)
                .set(Fields.LOCATION_X, location.getBlockX())
                .set(Fields.LOCATION_Y, location.getBlockY())
                .set(Fields.LOCATION_Z, location.getBlockZ())
                .returningResult(Fields.ID)
                .fetchOne();

        if (insert == null) {
            return FAILED_QUERY;
        }

        return insert.get(Fields.ID);
    }

    public static CompletionStage<Void> insertMinion(Minion minion) {
        return CompletableFuture.runAsync(() -> {
            World world = minion.location().getWorld();
            if (world == null) {
                return;
            }

            int worldId = worldId(world);
            if (worldId == FAILED_QUERY) {
                log.error("Failed worldId fetching!");
                return;
            }

            int locationId = locationId(worldId, minion.location());
            if (locationId == FAILED_QUERY) {
                log.error("Failed locationId fetching!");
                return;
            }

            DatabaseConnector.getInstance().context()
                    .insertInto(Tables.MINIONS)
                    .set(Fields.LOCATION_ID, locationId)
                    .set(Fields.OWNER_ID, minion.ownerId())
                    .set(Fields.TYPE_ID, minion.type().id())
                    .set(Fields.LEVEL, minion.level().id())
                    .set(Fields.CHARGE, minion.charge())
                    .set(Fields.FACING, minion.facing().ordinal())
                    .set(Fields.TOOL, minion.tool() == null || minion.tool().getType().isAir() ? null : WrappedItemStack.wrap(minion.tool()).serialize())
                    .set(Fields.EXTRA_DATA, MinionData.serialize(minion.extraData()))
                    .execute();
        }, databaseExecutor).exceptionallyAsync(throwable -> {
            log.error("An unexpected error occurred while inserting minion!", throwable);
            return null;
        });
    }

    public static CompletionStage<Integer> insertType(MinionType type) {
        Preconditions.checkNotNull(type, "Tried to insert null miniontype");
        return CompletableFuture.supplyAsync(() -> {
            Result<Record> select = DatabaseConnector.getInstance().context()
                    .select()
                    .from(Tables.TYPES)
                    .where(Fields.NAME.eq(type.name()))
                    .fetch();

            if (!select.isEmpty()) {
                Record record = select.get(0);
                LogUtils.debug("select record: {}", record);
                return (Integer) record.get("ID");
            }

            Record1<Integer> insert = DatabaseConnector.getInstance().context()
                    .insertInto(Tables.TYPES)
                    .set(Fields.NAME, type.name())
                    .returningResult(Fields.ID)
                    .fetchOne();

            if (insert == null) {
                return FAILED_QUERY;
            }

            return insert.get(Fields.ID);
        }).exceptionallyAsync(throwable -> {
            log.error("An unexpected error occurred while inserting minion type {}!", type.name(), throwable);
            return FAILED_QUERY;
        });
    }

    public static void stop() {
        try {
            databaseExecutor.shutdown();
            databaseExecutor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException exception) {
            log.error("An unexpected error occurred while stopping DataHandler!", exception);
        }
    }

    public static ExecutorService databaseExecutor() {
        return databaseExecutor;
    }
}
