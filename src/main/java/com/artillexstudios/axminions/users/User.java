package com.artillexstudios.axminions.users;

import java.util.UUID;

public record User(UUID uuid, String name, String texture, int extraSlots, int extraIslandSlots) {
}
