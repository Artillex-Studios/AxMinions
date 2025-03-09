package com.artillexstudios.axminions.minions.warnings;

import com.artillexstudios.axapi.packetentity.PacketEntity;
import com.artillexstudios.axminions.minions.Minion;
import net.kyori.adventure.text.Component;

public final class Warnings {
    private final Minion minion;
    private PacketEntity entity;
    private long previousUpdate = Long.MAX_VALUE;

    public Warnings(Minion minion) {
        this.minion = minion;
    }

    public void show(Component message) {
        if (this.entity != null) {
            return;
        }
    }

    public void clean() {
        if (this.entity != null) {
            this.entity.remove();
        }
    }
}
