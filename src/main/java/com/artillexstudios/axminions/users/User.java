package com.artillexstudios.axminions.users;

import com.artillexstudios.axminions.minions.Minion;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record User(int id, UUID uuid, String name, String texture, int minionCount, int extraSlots,
                   int extraIslandSlots, List<Minion> minions) {

    public User(int id, UUID uuid, String name, int extraSlots, int extraIslandSlots, int minionCount) {
        this(id, uuid, name, null, minionCount, extraSlots, extraIslandSlots, new ArrayList<>());
    }

    public User minionCount(int minionCount) {
        User user = new User(this.id, this.uuid, this.name, this.texture, minionCount, this.extraSlots, this.extraIslandSlots, this.minions);
        Users.load(user);
        return user;
    }

    public User texture(String texture) {
        User user = new User(this.id, this.uuid, this.name, texture, this.minionCount, this.extraSlots, this.extraIslandSlots, this.minions);
        Users.load(user);
        return user;
    }
}
