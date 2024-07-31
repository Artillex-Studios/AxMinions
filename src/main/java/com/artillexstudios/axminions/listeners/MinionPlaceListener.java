package com.artillexstudios.axminions.listeners;

import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.items.component.DataComponents;
import com.artillexstudios.axapi.items.nbt.CompoundTag;
import com.artillexstudios.axminions.database.DataHandler;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.MinionArea;
import com.artillexstudios.axminions.minions.MinionData;
import com.artillexstudios.axminions.minions.MinionType;
import com.artillexstudios.axminions.minions.MinionTypes;
import com.artillexstudios.axminions.minions.MinionWorldCache;
import com.artillexstudios.axminions.utils.Direction;
import com.artillexstudios.axminions.utils.LocationUtils;
import com.artillexstudios.axminions.utils.LogUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public final class MinionPlaceListener implements Listener {

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        LogUtils.debug("MinionPlaceListener - PlayerInteractEvent");
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            LogUtils.debug("Not right click block, but {}.", event.getAction());
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            LogUtils.debug("Clicked block is null.");
            return;
        }

        EquipmentSlot hand = event.getHand();
        if (hand == null) {
            LogUtils.debug("Hand is null!");
            return;
        }

        ItemStack itemStack = event.getPlayer().getInventory().getItem(hand);
        if (itemStack == null || itemStack.getType().isAir()) {
            LogUtils.debug("Item is either null, or air!");
            return;
        }

        WrappedItemStack wrappedItemStack = WrappedItemStack.wrap(itemStack);
        CompoundTag compoundTag = wrappedItemStack.get(DataComponents.customData());
        if (!compoundTag.contains("axminions_minion_type")) {
            LogUtils.debug("Does not contain miniontype");
            return;
        }

        event.setCancelled(true);
        String typeName = compoundTag.getString("axminions_minion_type");
        if (typeName == null || typeName.isBlank()) {
            LogUtils.warn("ItemStack in {}'s hand is an invalid minion; the miniontype is empty!", event.getPlayer().getName());
            return;
        }

        MinionType minionType = MinionTypes.parse(typeName);
        if (minionType == null) {
            LogUtils.warn("ItemStack in {}'s hand is an invalid minion; no miniontype with id {} is loaded!", typeName);
            return;
        }

        // TODO: level
        itemStack.setAmount(itemStack.getAmount() - 1);
        Location location = LocationUtils.toBlockCenter(clickedBlock.getRelative(event.getBlockFace()).getLocation());

        MinionArea area = MinionWorldCache.getArea(location.getWorld());
        if (area == null) {
            LogUtils.warn("{} attempted to place minion, but area is null!", event.getPlayer().getName());
            return;
        }

        Minion atLocation = area.getMinionAt(location);
        if (atLocation != null) {
            // TODO: Send message
            return;
        }

        // TODO: Database queries, etc..
        // TODO: ownerId
        MinionData data = new MinionData(0, minionType, Direction.NORTH, null, minionType.level(1), 0, null, null, new HashMap<>());
        Minion minion = new Minion(location, data);
        MinionWorldCache.add(minion);
        DataHandler.insertMinion(minion).thenRun(() -> {
            LogUtils.debug("Inserted minion!");
        });
        minion.spawn();
        area.startTicking(location.getChunk());
    }
}
