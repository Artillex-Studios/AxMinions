package com.artillexstudios.axminions.database;

import com.artillexstudios.axminions.config.Config;
import com.artillexstudios.axminions.utils.FileUtils;
import com.zaxxer.hikari.HikariConfig;
import org.jooq.SQLDialect;

public enum DatabaseType {
    H2(SQLDialect.H2) {
        @Override
        public HikariConfig getConfig() {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
            hikariConfig.setPoolName("axminions-database-pool");
            hikariConfig.setMaximumPoolSize(Config.database.pool.maximumPoolSize);
            hikariConfig.addDataSourceProperty("url", "jdbc:h2:./" + FileUtils.PLUGIN_DIRECTORY.toFile() + "/data;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;IGNORECASE=TRUE");
            return hikariConfig;
        }
    },
    SQLITE(SQLDialect.SQLITE) {
        @Override
        public HikariConfig getConfig() {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setDriverClassName("org.sqlite.JDBC");
            hikariConfig.setPoolName("axminions-database-pool");
            hikariConfig.setMaximumPoolSize(Config.database.pool.maximumPoolSize);
            hikariConfig.setJdbcUrl("jdbc:sqlite:" + FileUtils.PLUGIN_DIRECTORY.toFile() + "/data");
            return hikariConfig;
        }
    },
    MySQL(SQLDialect.MYSQL) {
        @Override
        public HikariConfig getConfig() {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setPoolName("axminions-database-pool");
            hikariConfig.setMaximumPoolSize(Config.database.pool.maximumPoolSize);
            hikariConfig.setMinimumIdle(Config.database.pool.minimumIdle);
            hikariConfig.setMaxLifetime(Config.database.pool.maximumLifetime);
            hikariConfig.setKeepaliveTime(Config.database.pool.keepaliveTime);
            hikariConfig.setConnectionTimeout(Config.database.pool.connectionTimeout);

            hikariConfig.setDriverClassName("com.mysql.jdbc.Driver");
            hikariConfig.setJdbcUrl("jdbc:mysql://" + Config.database.address + ":" + Config.database.port + "/" + Config.database.database);
            hikariConfig.addDataSourceProperty("user", Config.database.username);
            hikariConfig.addDataSourceProperty("password", Config.database.password);
            return hikariConfig;
        }
    };

    public static final DatabaseType[] entries = DatabaseType.values();
    private final SQLDialect dialect;

    DatabaseType(SQLDialect dialect) {
        this.dialect = dialect;
    }

    public static DatabaseType parse(String name) {
        for (DatabaseType entry : entries) {
            if (entry.name().equalsIgnoreCase(name)) {
                return entry;
            }
        }

        return DatabaseType.H2;
    }

    public SQLDialect getType() {
        return this.dialect;
    }

    public abstract HikariConfig getConfig();
}
