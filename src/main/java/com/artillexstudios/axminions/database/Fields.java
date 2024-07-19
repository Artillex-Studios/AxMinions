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
    public static final Field<Object> WORLD_UUID = DSL.field("world_uuid");
    public static final Field<Object> WORLD_ID = DSL.field("world_id");
    public static final Field<Object> LOCATION_X = DSL.field("location_x");
    public static final Field<Object> LOCATION_Y = DSL.field("location_y");
    public static final Field<Object> LOCATION_Z = DSL.field("location_z");
    public static final Field<Object> LOCATION_ID = DSL.field("location_id");
    public static final Field<Object> OWNER_ID = DSL.field("owner_id");
    public static final Field<Object> TYPE_ID = DSL.field("type_id");
    public static final Field<Object> LEVEL = DSL.field("level");
    public static final Field<Object> CHARGE = DSL.field("charge");
    public static final Field<Object> FACING = DSL.field("facing");
    public static final Field<Object> TOOL = DSL.field("tool");
    public static final Field<Object> EXTRA_DATA = DSL.field("extra_data");
}
