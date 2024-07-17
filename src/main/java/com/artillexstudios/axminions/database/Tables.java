package com.artillexstudios.axminions.database;

import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;

public final class Tables {
    public static final Table<Record> USERS = DSL.table("axminions_users");
    public static final Table<Record> TYPES = DSL.table("axminions_types");

}
