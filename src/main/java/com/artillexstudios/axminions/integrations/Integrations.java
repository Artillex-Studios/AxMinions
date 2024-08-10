package com.artillexstudios.axminions.integrations;

import com.artillexstudios.axminions.integrations.implementation.block.BlockIntegration;
import com.artillexstudios.axminions.integrations.implementation.stacker.StackerIntegration;
import com.artillexstudios.axminions.integrations.implementation.storage.StorageIntegration;

public final class Integrations {
    public static final BlockIntegration BLOCK = new BlockIntegration();
    public static final StackerIntegration STACKER = new StackerIntegration();
    public static final StorageIntegration STORAGE = new StorageIntegration();
}
