package com.artillexstudios.axminions.minions.warnings;

import com.artillexstudios.axapi.nms.NMSHandlers;
import com.artillexstudios.axapi.packetentity.PacketEntity;
import com.artillexstudios.axapi.packetentity.meta.entity.ArmorStandMeta;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axminions.config.Config;
import com.artillexstudios.axminions.minions.Minion;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.EntityType;

public final class Warnings {
    private final Minion minion;
    private PacketEntity entity;
    private boolean spawned = false;
    private Component current;

    public Warnings(Minion minion) {
        this.minion = minion;
    }

    public void show(Component message) {
        if (Config.debug) {
            LogUtils.debug("Show warning! Message: {}", message);
        }
        if (this.spawned && this.current == message) {
            if (Config.debug) {
                LogUtils.debug("This is already the message!");
            }
            return;
        }

        this.current = message;
        if (this.entity == null) {
            if (Config.debug) {
                LogUtils.debug("Creating new entity!");
            }
            this.entity = NMSHandlers.getNmsHandler().createEntity(EntityType.ARMOR_STAND, this.minion.location().clone().add(0, Config.warningHologramYOffset, 0));
        }

        ArmorStandMeta meta = (ArmorStandMeta) this.entity.meta();
        meta.customNameVisible(true);
        meta.name(message);
        meta.invisible(true);
        meta.marker(true);

        if (!this.spawned) {
            this.entity.spawn();
            this.spawned = true;
            if (Config.debug) {
                LogUtils.debug("Spawned new entity!");
            }
        }
    }

    public void clean() {
        if (this.entity != null && this.spawned) {
            this.entity.remove();
            this.spawned = false;
            if (Config.debug) {
                LogUtils.debug("Removed warning entity!");
            }
        }
    }
}
