package com.artillexstudios.axminions.users;

import com.artillexstudios.axminions.minions.Minion;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record User(int id, UUID uuid, String name, String texture, String signature, int minionCount, int extraSlots,
                   int extraIslandSlots, List<Minion> minions) {

    public User(int id, UUID uuid, String name, String texture, String signature, int extraSlots, int extraIslandSlots, long minionCount) {
        this(id, uuid, name, texture, signature, (int) minionCount, extraSlots, extraIslandSlots, new ArrayList<>());
    }

    public User minionCount(int minionCount) {
        User user = new User(this.id, this.uuid, this.name, this.texture, this.signature, minionCount, this.extraSlots, this.extraIslandSlots, this.minions);
        Users.load(user);
        return user;
    }

    public User texture(String texture, String signature) {
        User user = new User(this.id, this.uuid, this.name, texture, signature, this.minionCount, this.extraSlots, this.extraIslandSlots, this.minions);
        Users.load(user);
        return user;
    }
}
