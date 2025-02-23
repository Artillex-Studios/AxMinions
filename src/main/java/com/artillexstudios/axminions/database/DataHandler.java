package com.artillexstudios.axminions.database;

import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.nms.wrapper.ServerPlayerWrapper;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.AsyncUtils;
import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axminions.config.Config;
import com.artillexstudios.axminions.minions.Level;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.MinionData;
import com.artillexstudios.axminions.minions.MinionType;
import com.artillexstudios.axminions.minions.MinionTypes;
import com.artillexstudios.axminions.minions.MinionWorldCache;
import com.artillexstudios.axminions.users.User;
import com.artillexstudios.axminions.utils.Direction;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.IntLongPair;
import it.unimi.dsi.fastutil.longs.LongLongPair;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.impl.SQLDataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class DataHandler {
    private static final int FAILED_QUERY = -3042141;
    private final DatabaseConnector connector;

    public DataHandler(DatabaseConnector connector) {
        this.connector = connector;
    }

    public CompletionStage<Void> setup() {
        ArrayList<CompletableFuture<Integer>> futures = new ArrayList<>();

        CompletionStage<Integer> types = this.connector.context().createTableIfNotExists(Tables.TYPES)
                .column(Fields.ID, SQLDataType.SMALLINT.identity(true))
                .column(Fields.NAME, SQLDataType.VARCHAR(64))
                .primaryKey(Fields.ID)
                .executeAsync(AsyncUtils.executor())
                .exceptionallyAsync(throwable -> {
                    LogUtils.error("An unexpected error occurred while running type table creation query!", throwable);
                    return FAILED_QUERY;
                });

        futures.add(types.toCompletableFuture());
        CompletionStage<Integer> users = this.connector.context().createTableIfNotExists(Tables.USERS)
                .column(Fields.ID, SQLDataType.INTEGER.identity(true))
                .column(Fields.UUID, SQLDataType.UUID)
                .column(Fields.NAME, SQLDataType.VARCHAR(16))
                .column(Fields.EXTRA_SLOTS, SQLDataType.INTEGER.default_(0))
                .column(Fields.ISLAND_SLOTS, SQLDataType.INTEGER.default_(0))
                .primaryKey(Fields.ID)
                .executeAsync(AsyncUtils.executor())
                .exceptionallyAsync(throwable -> {
                    LogUtils.error("An unexpected error occurred while running user table creation query!", throwable);
                    return FAILED_QUERY;
                });

        futures.add(users.toCompletableFuture());

        CompletionStage<Integer> worlds = this.connector.context().createTableIfNotExists(Tables.WORLDS)
                .column(Fields.ID, SQLDataType.SMALLINT.identity(true))
                .column(Fields.WORLD_UUID, SQLDataType.UUID)
                .primaryKey(Fields.ID)
                .executeAsync(AsyncUtils.executor())
                .exceptionallyAsync(throwable -> {
                    LogUtils.error("An unexpected error occurred while running world table creation query!", throwable);
                    return FAILED_QUERY;
                }, AsyncUtils.executor());

        futures.add(worlds.toCompletableFuture());

        CompletionStage<Integer> locations = this.connector.context().createTableIfNotExists(Tables.LOCATIONS)
                .column(Fields.ID, SQLDataType.INTEGER.identity(true))
                .column(Fields.WORLD_ID, SQLDataType.SMALLINT)
                .column(Fields.LOCATION_X, SQLDataType.INTEGER)
                .column(Fields.LOCATION_Y, SQLDataType.INTEGER)
                .column(Fields.LOCATION_Z, SQLDataType.INTEGER)
                .primaryKey(Fields.ID)
                .executeAsync(AsyncUtils.executor())
                .exceptionallyAsync(throwable -> {
                    LogUtils.error("An unexpected error occurred while running locations table creation query!", throwable);
                    return FAILED_QUERY;
                }, AsyncUtils.executor());

        futures.add(locations.toCompletableFuture());

        CompletionStage<Integer> minions = this.connector.context().createTableIfNotExists(Tables.MINIONS)
                .column(Fields.ID, SQLDataType.INTEGER.identity(true))
                .column(Fields.LOCATION_ID, SQLDataType.INTEGER)
                .column(Fields.OWNER_ID, SQLDataType.INTEGER)
                .column(Fields.TYPE_ID, SQLDataType.SMALLINT)
                .column(Fields.LEVEL, SQLDataType.SMALLINT)
                .column(Fields.CHARGE, SQLDataType.BIGINT)
                .column(Fields.FACING, SQLDataType.TINYINT)
                .column(Fields.TOOL, SQLDataType.CLOB)
                .column(Fields.EXTRA_DATA, SQLDataType.CLOB)
                .primaryKey(Fields.ID)
                .executeAsync(AsyncUtils.executor())
                .exceptionallyAsync(throwable -> {
                    LogUtils.error("An unexpected error occurred while running minions table creation query!", throwable);
                    return FAILED_QUERY;
                }, AsyncUtils.executor());

        futures.add(minions.toCompletableFuture());
        if (Config.database.type == DatabaseType.SQLITE) {
            CompletableFuture<Integer> pragma = new CompletableFuture<>();
            AsyncUtils.executor().submit(() -> {
                this.connector.context().fetch("PRAGMA journal_mode=WAL;");
                this.connector.context().execute("PRAGMA synchronous = off;");
                this.connector.context().execute("PRAGMA page_size = 32768;");
                this.connector.context().fetch("PRAGMA mmap_size = 30000000000;");
                pragma.complete(1);
            });
            futures.add(pragma);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    public CompletionStage<User> loadUser(Player player) {
        String texture = ServerPlayerWrapper.wrap(player).textures(player).texture();
        if (Config.debug) {
            LogUtils.debug("Updating user! Texture: {}", texture == null ? "null" : texture);
        }
        return CompletableFuture.supplyAsync(() -> {
            Result<Record> select = this.connector.context()
                    .select()
                    .from(Tables.USERS)
                    .where(Fields.UUID.eq(player.getUniqueId()))
                    .limit(1)
                    .fetch();

            if (!select.isEmpty()) {
                Record record = select.get(0);
                if (Config.debug) {
                    LogUtils.debug("User data select record: {}", record);
                }
                int ownerId = record.get(Fields.ID);

                Result<Record1<Integer>> minionSelect = this.connector.context()
                        .selectCount()
                        .from(Tables.MINIONS)
                        .where(Fields.OWNER_ID.eq(ownerId))
                        .limit(1)
                        .fetch();

                if (!minionSelect.isEmpty()) {
                    Record minionRecord = minionSelect.get(0);
                    int minionCount = minionRecord.get(0, int.class);

                    return new User(ownerId, player.getUniqueId(), player.getName(), texture, minionCount, record.get(Fields.EXTRA_SLOTS, int.class), record.get(Fields.ISLAND_SLOTS, int.class), new ArrayList<>());
                }

                return new User(ownerId, player.getUniqueId(), player.getName(), texture, 0, record.get(Fields.EXTRA_SLOTS, int.class), record.get(Fields.ISLAND_SLOTS, int.class), new ArrayList<>());
            }

            Record1<Integer> insert = this.connector.context()
                    .insertInto(Tables.USERS)
                    .set(Fields.UUID, player.getUniqueId())
                    .set(Fields.NAME, player.getName())
                    .set(Fields.EXTRA_SLOTS, 0)
                    .set(Fields.ISLAND_SLOTS, 0)
                    .returningResult(Fields.ID)
                    .fetchOne();

            if (insert == null) {
                return null;
            }

            return new User(insert.get(Fields.ID), player.getUniqueId(), player.getName(), texture, 0, 0, 0, new ArrayList<>());
        }, AsyncUtils.executor()).exceptionallyAsync(throwable -> {
            LogUtils.error("An unexpected error occurred while updating user {}!", player.getName(), throwable);
            return null;
        }, AsyncUtils.executor());
    }

    public short worldId(World world) {
        Result<Record> select = this.connector.context()
                .select()
                .from(Tables.WORLDS)
                .where(Fields.WORLD_UUID.eq(world.getUID()))
                .limit(1)
                .fetch();

        if (!select.isEmpty()) {
            Record record = select.get(0);
            if (Config.debug) {
                LogUtils.debug("World select record: {}", record);
            }
            return record.get(Fields.ID, short.class);
        }

        Record1<Integer> insert = this.connector.context()
                .insertInto(Tables.WORLDS)
                .set(Fields.WORLD_UUID, world.getUID())
                .returningResult(Fields.ID)
                .fetchOne();

        if (insert == null) {
            return (short) FAILED_QUERY;
        }

        return insert.get(Fields.ID, short.class);
    }

    public int locationId(int world, Location location) {
        Result<Record> select = this.connector.context()
                .select()
                .from(Tables.LOCATIONS)
                .where(Fields.WORLD_ID.eq(world)
                        .and(Fields.LOCATION_X.eq(location.getBlockX()))
                        .and(Fields.LOCATION_Y.eq(location.getBlockY()))
                        .and(Fields.LOCATION_Z.eq(location.getBlockZ())))
                .limit(1)
                .fetch();

        if (!select.isEmpty()) {
            Record record = select.get(0);
            if (Config.debug) {
                LogUtils.debug("Location select record: {}", record);
            }
            return record.get(Fields.ID);
        }

        Record1<Integer> insert = this.connector.context()
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

    public String minionType(short id) {
        return this.connector.context()
                .select()
                .from(Tables.TYPES)
                .where(Fields.ID.eq((int) id))
                .limit(1)
                .fetchSingle(Fields.NAME);
    }

    public CompletionStage<Void> insertMinion(Minion minion) {
        return CompletableFuture.runAsync(() -> {
            World world = minion.location().getWorld();
            if (world == null) {
                return;
            }

            int worldId = this.worldId(world);
            if (worldId == FAILED_QUERY) {
                LogUtils.error("Failed worldId fetching!");
                return;
            }

            int locationId = this.locationId(worldId, minion.location());
            if (locationId == FAILED_QUERY) {
                LogUtils.error("Failed locationId fetching!");
                return;
            }

            this.connector.context()
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
        }, AsyncUtils.executor()).exceptionallyAsync(throwable -> {
            LogUtils.error("An unexpected error occurred while inserting minion!", throwable);
            return null;
        }, AsyncUtils.executor());
    }

    public CompletionStage<IntLongPair> loadMinions(World world) {
        return CompletableFuture.supplyAsync(() -> {
            int loadedMinions = 0;
            long start = System.nanoTime();
            int worldId = this.worldId(world);
            if (worldId == FAILED_QUERY) {
                LogUtils.error("Failed worldId fetching when loading minions!");
                return IntLongPair.of(0, 0);
            }

            List<Minion> toLoad = new ArrayList<>();
            Result<Record> locations = this.connector.context()
                    .select()
                    .from(Tables.LOCATIONS)
                    .where(Fields.WORLD_ID.eq(worldId))
                    .fetch();

            for (Record location : locations) {
                int id = location.get(Fields.ID);
                Result<Record> minions = this.connector.context()
                        .select()
                        .from(Tables.MINIONS)
                        .where(Fields.LOCATION_ID.eq(id))
                        .limit(1)
                        .fetch();

                if (minions.isEmpty()) {
                    continue;
                }

                Record record = minions.get(0);
                short typeId = record.get(Fields.TYPE_ID, short.class);
                int x = location.get(Fields.LOCATION_X, int.class);
                int y = location.get(Fields.LOCATION_Y, int.class);
                int z = location.get(Fields.LOCATION_Z, int.class);
                MinionType type = MinionTypes.parse(typeId);
                if (type == null) {
                    LogUtils.warn("Failed to load minion in world {} at ({};{};{})! Unregistered miniontype: {}", world.getName(), x, y, z, minionType(typeId));
                    continue;
                }

                int ownerId = record.get(Fields.OWNER_ID, int.class);
                short level = record.get(Fields.LEVEL, short.class);
                long charge = record.get(Fields.CHARGE, long.class);
                byte facing = record.get(Fields.FACING, byte.class);
                byte[] tool = record.get(Fields.TOOL, byte[].class);
                String extraData = record.get(Fields.EXTRA_DATA, String.class);
                Level minionLevel = type.level(level);

                if (minionLevel == null) {
                    LogUtils.warn("Failed to load minion in world {} at ({};{};{})! Level does not exist: {}", world.getName(), x, y, z, level);
                    continue;
                }

                MinionData data = new MinionData(ownerId, type, Direction.entries[facing], null, minionLevel, charge, tool == null ? new ItemStack(Material.AIR) : WrappedItemStack.wrap(tool).toBukkit(), null, MinionData.deserialize(extraData));
                Minion minion = new Minion(new Location(world, x + 0.5, y, z + 0.5), data);
                toLoad.add(minion);
                minion.spawn();
                loadedMinions++;
            }

            CompletableFuture.runAsync(() -> {
                MinionWorldCache.addAll(toLoad);
            }, command -> Scheduler.get().run(command)).join();
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
            Result<Record> select = this.connector.context()
                    .select()
                    .from(Tables.TYPES.getName().toLowerCase(Locale.ENGLISH))
                    .where(Fields.NAME.eq(type.name()))
                    .limit(1)
                    .fetch();

            if (!select.isEmpty()) {
                Record record = select.get(0);
                if (Config.debug) {
                    LogUtils.debug("select record: {}", record);
                }
                return record.get(Fields.ID, short.class);
            }

            Record1<Integer> insert = this.connector.context()
                    .insertInto(Tables.TYPES)
                    .set(Fields.NAME, type.name())
                    .returningResult(Fields.ID)
                    .fetchOne();

            if (insert == null) {
                return (short) FAILED_QUERY;
            }

            return insert.get(Fields.ID, short.class);
        }).exceptionallyAsync(throwable -> {
            LogUtils.error("An unexpected error occurred while inserting minion type {}!", type.name(), throwable);
            return (short) FAILED_QUERY;
        }, AsyncUtils.executor());
    }

    public CompletionStage<LongLongPair> saveMinions(Collection<Minion> minions) {
        Preconditions.checkNotNull(minions, "Tried to save null minions");
        return CompletableFuture.supplyAsync(() -> {
            long start = System.nanoTime();
            List<Query> queries = new ArrayList<>();
            for (Minion minion : minions) {
                if (!minion.needsSaving()) {
                    continue;
                }

                Location location = minion.location();
                World world = location.getWorld();
                if (world == null) {
                    continue;
                }

                int locationId;
                try {
                    int worldId = worldId(world);
                    locationId = locationId(worldId, location);
                } catch (Exception exception) {
                    LogUtils.warn("An exception occurred while saving minion {}", minion, exception);
                    continue;
                }

                Query query = this.connector.context()
                        .update(Tables.MINIONS)
                        .set(Fields.LEVEL, minion.level().id())
                        .set(Fields.CHARGE, minion.charge())
                        .set(Fields.FACING, minion.facing().ordinal())
                        .set(Fields.TOOL, minion.tool().getType().isAir() ? null : WrappedItemStack.wrap(minion.tool()).serialize())
                        .set(Fields.EXTRA_DATA, MinionData.serialize(minion.extraData()))
                        .where(Fields.LOCATION_ID.eq(locationId));

                minion.save();
                queries.add(query);
            }

            this.connector.context()
                    .batch(queries)
                    .execute();

            if (Config.database.type == DatabaseType.SQLITE) {
                this.connector.context().execute("PRAGMA wal_checkpoint(FULL);");
            }

            long took = System.nanoTime() - start;
            return LongLongPair.of(queries.size(), took);
        }).exceptionallyAsync(throwable -> {
            LogUtils.error("An unexpected error occurred while saving minions!", throwable);
            return LongLongPair.of(FAILED_QUERY, FAILED_QUERY);
        }, AsyncUtils.executor());
    }
}
