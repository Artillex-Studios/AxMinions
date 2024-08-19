package com.artillexstudios.axminions.users;

import java.util.UUID;

public record User(int id, UUID uuid, String name, String texture, int extraSlots, int extraIslandSlots) {
}
