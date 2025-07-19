package com.artillexstudios.axminions.minions;

import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.items.component.DataComponents;
import com.artillexstudios.axapi.items.component.type.ProfileProperties;
import com.artillexstudios.axapi.items.nbt.CompoundTag;
import com.artillexstudios.axapi.nms.NMSHandlers;
import com.artillexstudios.axapi.packetentity.PacketEntity;
import com.artillexstudios.axapi.packetentity.meta.entity.ArmorStandMeta;
import com.artillexstudios.axapi.packetentity.meta.serializer.Accessors;
import com.artillexstudios.axapi.utils.AsyncUtils;
import com.artillexstudios.axapi.utils.EquipmentSlot;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axminions.config.Config;
import com.artillexstudios.axminions.exception.MinionWarningException;
import com.artillexstudios.axminions.integrations.Integrations;
import com.artillexstudios.axminions.minions.skins.Skin;
import com.artillexstudios.axminions.minions.warnings.Warnings;
import com.artillexstudios.axminions.users.User;
import com.artillexstudios.axminions.users.Users;
import com.artillexstudios.axminions.utils.Direction;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Minion {
    private static final UUID PROFILE_UUID = UUID.randomUUID();
    private static final ItemStack AIR = new ItemStack(Material.AIR);
    private final int id;
    private final Location location;
    private final PacketEntity entity;
    private final AtomicBoolean needsSaving = new AtomicBoolean(false);
    private final Warnings warnings = new Warnings(this);
    private int tick = 0;
    private int armTick = 0;
    private boolean ticking = false;
    private MinionData minionData;

    public Minion(int id, Location location, MinionData data) {
        this.id = id;
        this.location = location;
        this.minionData = data;
        this.entity = NMSHandlers.getNmsHandler().createEntity(EntityType.ARMOR_STAND, location);
        ArmorStandMeta meta = (ArmorStandMeta) this.entity.meta();
        meta.small(true);
        meta.showArms(true);
        this.applySkin();

        if (!Config.requireTool) {
            this.minionData.withTool(this.minionData.type().tool());
        }
    }

    public void tick() {
        this.tick += Config.tickFrequency;
        if (this.tick < this.minionData.level().actionTicks() * Config.tickFrequency) {
            if (Config.showHandAnimation) {
                AsyncUtils.run(() -> {
                    if (this.armTick >= (20 * Config.tickFrequency)) return;
                    ArmorStandMeta meta = (ArmorStandMeta) this.entity.meta();
                    meta.metadata().set(Accessors.RIGHT_ARM_ROTATION, new EulerAngle((-2 + ((double) (this.armTick * Config.tickFrequency) / 10)), 0, 0));
                    this.armTick += (2 * Config.tickFrequency);
                }, Config.asyncHandAnimation);
            }
            return;
        }

        boolean cleanWarnings = true;
        try {
            this.tick = 0;
            if (Config.showHandAnimation & this.minionData.type().tick(this)) {
                this.armTick = 0;
            }
        } catch (MinionWarningException exception) {
            cleanWarnings = false;
        }

        if (cleanWarnings) {
            if (Config.debug) {
                LogUtils.debug("Cleaning warnings!");
            }
            this.warnings.clean();
        }

        Integrations.STORAGE.flush(this.minionData.linkedChest());
        Integrations.STORAGE.flushDrops(this.location);
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
                ProfileProperties properties = new ProfileProperties(PROFILE_UUID, "axminions");
                properties.put("textures", new ProfileProperties.Property("textures", this.extraData().get("owner_texture"), this.extraData().get("owner_signature")));
                wrappedItemStack = wrappedItemStack.copy();
                wrappedItemStack.set(DataComponents.profile(), properties);
            }

            this.entity.setItem(entry.getKey(), wrappedItemStack);
        }
    }

    @Nullable
    public User owner() {
        return Users.get(this.ownerId());
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

    // Remove removes the minion from the database,
    // while destroy removes the minion from the world
    public void remove() {
        this.destroy();
    }

    public void destroy() {
        this.ticking = false;
        this.entity.remove();
    }

    public Warnings warnings() {
        return this.warnings;
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
        if (Config.debug) {
            LogUtils.debug("Set ticking to: {}", ticking);
        }
        this.ticking = ticking;
    }

    public int id() {
        return this.id;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Minion minion)) {
            return false;
        }

        return this.id == minion.id;
    }

    @Override
    public int hashCode() {
        return this.id;
    }
}
