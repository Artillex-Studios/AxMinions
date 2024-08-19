package com.artillexstudios.axminions.minions;

import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.items.component.DataComponents;
import com.artillexstudios.axapi.items.component.ProfileProperties;
import com.artillexstudios.axapi.items.nbt.CompoundTag;
import com.artillexstudios.axapi.nms.NMSHandlers;
import com.artillexstudios.axapi.packetentity.PacketEntity;
import com.artillexstudios.axapi.packetentity.meta.entity.ArmorStandMeta;
import com.artillexstudios.axapi.packetentity.meta.serializer.Accessors;
import com.artillexstudios.axapi.utils.EquipmentSlot;
import com.artillexstudios.axminions.config.Config;
import com.artillexstudios.axminions.integrations.Integrations;
import com.artillexstudios.axminions.minions.skins.Skin;
import com.artillexstudios.axminions.utils.AsyncUtils;
import com.artillexstudios.axminions.utils.Direction;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Minion {
    private static final ItemStack AIR = new ItemStack(Material.AIR);
    private final Location location;
    private final PacketEntity entity;
    private final AtomicBoolean needsSaving = new AtomicBoolean(false);
    private int tick = 0;
    private int armTick = 0;
    private boolean ticking = false;
    private MinionData minionData;

    public Minion(Location location, MinionData data) {
        this.location = location;
        this.minionData = data;
        this.entity = NMSHandlers.getNmsHandler().createEntity(EntityType.ARMOR_STAND, location);
        ArmorStandMeta meta = (ArmorStandMeta) this.entity.meta();
        meta.small(true);
        meta.showArms(true);
        this.applySkin();
    }

    public void tick() {
        this.tick += Config.TICK_FREQUENCY;
        if (this.tick < this.minionData.level().actionTicks() * Config.TICK_FREQUENCY) {
            if (Config.SHOW_HAND_ANIMATION) {
                AsyncUtils.run(() -> {
                    if (this.armTick >= 20 * Config.TICK_FREQUENCY) return;
                    ArmorStandMeta meta = (ArmorStandMeta) this.entity.meta();
                    meta.metadata().set(Accessors.RIGHT_ARM_ROTATION, new EulerAngle((-2 + ((double) this.armTick / 10)), 0, 0));
                    this.armTick += (2 * Config.TICK_FREQUENCY);
                }, Config.ASYNC_HAND_ANIMATION);
            }
            return;
        }

        this.tick = 0;
        if (Config.SHOW_HAND_ANIMATION & this.minionData.type().tick(this)) {
            this.armTick = 0;
        }

        Integrations.STORAGE.flush(this.minionData.linkedChest());
    }

    public void refresh() {

    }

    public void skin(Skin skin) {
        this.minionData = this.minionData.withSkin(skin);

        this.applySkin();
        this.needsSaving.set(true);
    }

    private void applySkin() {
        Skin skin = this.minionData.skin();
        if (skin == null) {
            skin = this.minionData.level().skin();
        }

        if (skin == null) {
            return;
        }

        for (Map.Entry<EquipmentSlot, WrappedItemStack> entry : skin.items().entrySet()) {
            WrappedItemStack wrappedItemStack = entry.getValue();
            CompoundTag tag = wrappedItemStack.get(DataComponents.customData());
            if (tag.contains("axminions_ownerskin") && this.extraData().containsKey("owner_texture")) {
                ProfileProperties properties = new ProfileProperties(UUID.randomUUID(), "axminions");
                properties.put("textures", new ProfileProperties.Property("textures", this.extraData().get("owner_texture"), null));
                wrappedItemStack = wrappedItemStack.copy();
                wrappedItemStack.set(DataComponents.profile(), properties);
            }

            this.entity.setItem(entry.getKey(), wrappedItemStack);
        }
    }

    public Location linkedChest() {
        return this.minionData.linkedChest();
    }

    public Map<String, String> extraData() {
        return this.minionData.extraData();
    }

    public Direction facing() {
        return this.minionData.direction();
    }

    public long charge() {
        return this.minionData.charge();
    }

    public int ownerId() {
        return this.minionData.ownerId();
    }

    public ItemStack tool() {
        return this.minionData.tool() == null ? AIR : this.minionData.tool();
    }

    public Level level() {
        return this.minionData.level();
    }

    public Skin skin() {
        return this.minionData.skin();
    }

    public MinionType type() {
        return this.minionData.type();
    }

    public void spawn() {
        this.entity.spawn();
    }

    public void remove() {
        this.entity.remove();
    }

    public boolean needsSaving() {
        return this.needsSaving.get();
    }

    public void save() {
        this.needsSaving.set(false);
    }

    public Location location() {
        return this.location;
    }

    public boolean ticking() {
        return this.ticking;
    }

    public void ticking(boolean ticking) {
        this.ticking = ticking;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Minion minion)) return false;

        return this.tick == minion.tick && Objects.equals(this.location, minion.location);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(this.location);
        result = 31 * result + this.tick;
        return result;
    }
}
