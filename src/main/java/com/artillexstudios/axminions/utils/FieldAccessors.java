package com.artillexstudios.axminions.utils;

import com.artillexstudios.axapi.reflection.FastFieldAccessor;
import com.artillexstudios.axapi.utils.ItemBuilder;

public final class FieldAccessors {
    public static final FastFieldAccessor STACK_ACCESSOR = FastFieldAccessor.forClassField(ItemBuilder.class, "stack");
}
