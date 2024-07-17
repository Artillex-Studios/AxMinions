package com.artillexstudios.axminions.database;

import com.artillexstudios.axminions.config.Config;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.Nullable;
import org.jooq.ConnectionProvider;
import org.jooq.DSLContext;
import org.jooq.conf.ParseNameCase;
import org.jooq.conf.RenderKeywordCase;
import org.jooq.conf.RenderNameCase;
import org.jooq.conf.Settings;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public final class DatabaseConnector {
    private static final DatabaseConnector INSTANCE = new DatabaseConnector();
    private static final Logger log = LoggerFactory.getLogger(DatabaseConnector.class);
    private final DSLContext context;
    private final HikariDataSource dataSource;

    private DatabaseConnector() {
        this.dataSource = new HikariDataSource(Config.DATABASE_TYPE.getConfig());

        Settings settings = new Settings()
                .withRenderNameCase(RenderNameCase.LOWER)
                .withRenderKeywordCase(RenderKeywordCase.UPPER)
                .withParseNameCase(ParseNameCase.LOWER);

        this.context = DSL.using(new ConnectionProvider() {

            @Override
            public @Nullable Connection acquire() throws DataAccessException {
                return DatabaseConnector.this.connection();
            }

            @Override
            public void release(Connection connection) throws DataAccessException {
                try {
                    connection.close();
                } catch (SQLException exception) {
                    log.error("An exception occurred while releasing connection!", exception);
                }
            }
        }, Config.DATABASE_TYPE.getType(), settings);
    }

    public DSLContext context() {
        return this.context;
    }

    public Connection connection() {
        try {
            return this.dataSource.getConnection();
        } catch (SQLException exception) {
            log.error("An unexpected error occurred when getting connection!", exception);
            throw new RuntimeException(exception);
        }
    }

    public void close() {
        this.dataSource.close();
    }

    public static DatabaseConnector getInstance() {
        return INSTANCE;
    }
}
