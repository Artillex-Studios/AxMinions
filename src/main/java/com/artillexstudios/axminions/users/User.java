package com.artillexstudios.axminions.users;

import com.artillexstudios.axminions.minions.Minion;

import java.util.List;
import java.util.UUID;

public record User(int id, UUID uuid, String name, String texture, int minionCount, int extraSlots, int extraIslandSlots, List<Minion> minions) {

    public void minionCount(int minionCount) {
        Users.load(new User(this.id, this.uuid, this.name, this.texture, minionCount, this.extraSlots, this.extraIslandSlots, this.minions));
    }
}
