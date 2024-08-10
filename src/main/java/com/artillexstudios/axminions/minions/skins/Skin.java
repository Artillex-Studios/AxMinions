package com.artillexstudios.axminions.minions.skins;

import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.items.component.DataComponents;
import com.artillexstudios.axapi.items.nbt.CompoundTag;
import com.artillexstudios.axapi.utils.EquipmentSlot;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axminions.utils.FieldAccessors;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public record Skin(String id, Map<EquipmentSlot, WrappedItemStack> items) {
    private static final WrappedItemStack AIR = WrappedItemStack.wrap(new ItemStack(Material.AIR));
    private static final EquipmentSlot[] equipmentSlots = EquipmentSlot.values();
    private static final Logger log = LoggerFactory.getLogger(Skin.class);

    public Skin(String id, Map<EquipmentSlot, WrappedItemStack> items) {
        this.id = id;
        this.items = Map.copyOf(items);
    }

    public static Skin of(String id, List<Map<Object, Object>> mapList) {
        EnumMap<EquipmentSlot, WrappedItemStack> equipment = new EnumMap<>(EquipmentSlot.class);

        for (Map<?, ?> map : mapList) {
            String slot = (String) map.get("slot");
            if (slot == null || slot.isBlank()) {
                log.warn("Found invalid skin configuration in skin configuration {}: Missing/invalid slot!", id);
                continue;
            }

            LinkedHashMap<Object, Object> section = (LinkedHashMap<Object, Object>) map.get("item");
            if (section == null) {
                log.warn("Found invalid skin configuration in skin configuration {}: Missing item configuration!", id);
                continue;
            }

            Optional<EquipmentSlot> parsed = parse(slot);
            if (parsed.isEmpty()) {
                log.warn("Found invalid skin configuration in skin configuration {}: Invalid slot name {}! Valid slot names: {}", id, slot, Arrays.toString(equipmentSlots));
                continue;
            }

            EquipmentSlot equipmentSlot = parsed.get();
            if (equipment.containsKey(equipmentSlot)) {
                log.warn("Found invalid skin configuration in skin configuration {}: Duplicate slot!", id);
                continue;
            }

            AtomicBoolean ownerSkin = new AtomicBoolean();
            Optional.ofNullable(section.get("texture")).ifPresent(texture -> {
                ownerSkin.set(texture.equals("<owner>"));
            });

            WrappedItemStack wrapped = FieldAccessors.STACK_ACCESSOR.get(new ItemBuilder(section));
            CompoundTag tag = wrapped.get(DataComponents.customData());
            tag.putBoolean("axminions_ownerskin", ownerSkin.get());
            wrapped.set(DataComponents.customData(), tag);

            equipment.put(equipmentSlot, wrapped);
        }

        for (EquipmentSlot equipmentSlot : equipmentSlots) {
            if (equipment.containsKey(equipmentSlot)) {
                continue;
            }

            equipment.put(equipmentSlot, AIR);
        }

        return new Skin(id, equipment);
    }

    private static Optional<EquipmentSlot> parse(String slot) {
        for (EquipmentSlot entry : equipmentSlots) {
            if (entry.name().equalsIgnoreCase(slot)) {
                return Optional.of(entry);
            }
        }

        return Optional.empty();
    }
}
