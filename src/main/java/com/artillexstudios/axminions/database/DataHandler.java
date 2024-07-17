package com.artillexstudios.axminions.database;

import com.artillexstudios.axapi.data.ThreadedExecutor;
import com.artillexstudios.axapi.nms.NMSHandlers;
import com.artillexstudios.axminions.minions.MinionType;
import com.artillexstudios.axminions.utils.LogUtils;
import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class DataHandler {
    private static final ThreadedExecutor databaseExecutor = new ThreadedExecutor("AxMinions-Database-Thread");
    private static final int FAILED_QUERY = -3042141;
    private static final Logger log = LoggerFactory.getLogger(DataHandler.class);

    public static CompletionStage<Void> setup() {
        CompletableFuture<Integer>[] futures = new CompletableFuture[2];

        CompletionStage<Integer> types = DatabaseConnector.getInstance().context().createTableIfNotExists(Tables.TYPES)
                .column(Fields.ID, SQLDataType.INTEGER.identity(true))
                .column(Fields.NAME, SQLDataType.VARCHAR(64))
                .primaryKey(Fields.ID)
                .executeAsync(databaseExecutor)
                .exceptionallyAsync(throwable -> {
                    log.error("An unexpected error occurred while running type table creation query!", throwable);
                    return FAILED_QUERY;
                });

        futures[0] = types.toCompletableFuture();
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

        futures[1] = users.toCompletableFuture();
        return CompletableFuture.allOf(futures);
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

    public static ThreadedExecutor databaseExecutor() {
        return databaseExecutor;
    }
}
