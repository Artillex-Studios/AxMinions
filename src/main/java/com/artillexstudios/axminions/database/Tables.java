package com.artillexstudios.axminions.database;

import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;

public final class Tables {
    public static final Table<Record> USERS = DSL.table("axminions_users");
    public static final Table<Record> TYPES = DSL.table("axminions_types");
    public static final Table<Record> WORLDS = DSL.table("axminions_worlds");
    public static final Table<Record> LOCATIONS = DSL.table("axminions_locations");
    public static final Table<Record> MINIONS = DSL.table("axminions_minions");
}
