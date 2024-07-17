package com.artillexstudios.axminions.database;

import org.jooq.Field;
import org.jooq.impl.DSL;

public final class Fields {
    public static final Field<Integer> ID = DSL.field("id", Integer.class);
    public static final Field<Object> UUID = DSL.field("uuid");
    public static final Field<String> NAME = DSL.field("name", String.class);
    public static final Field<Object> TEXTURE = DSL.field("texture");
    public static final Field<Object> EXTRA_SLOTS = DSL.field("extra_slots");
    public static final Field<Object> ISLAND_SLOTS = DSL.field("island_slots");
}
