package com.artillexstudios.axminions.users;

import com.artillexstudios.axminions.minions.Minion;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record User(Integer id, UUID uuid, String name, String texture, String signature, Integer minionCount, Integer extraSlots,
                   Integer extraIslandSlots, List<Minion> minions) {

    public User(Integer id, UUID uuid, String name, String texture, String signature, Integer extraSlots, Integer extraIslandSlots, Long minionCount) {
        this(id, uuid, name, texture, signature, minionCount.intValue(), extraSlots, extraIslandSlots, new ArrayList<>());
    }

    public User(Integer id, UUID uuid, String name, Integer extraSlots, Integer extraIslandSlots, Long minionCount) {
        this(id, uuid, name, null, null, minionCount.intValue(), extraSlots, extraIslandSlots, new ArrayList<>());
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
