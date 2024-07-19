package com.artillexstudios.axminions.command;

import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.utils.EquipmentSlot;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axminions.AxMinionsPlugin;
import com.artillexstudios.axminions.config.Language;
import com.artillexstudios.axminions.minions.Level;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.MinionArea;
import com.artillexstudios.axminions.minions.MinionData;
import com.artillexstudios.axminions.minions.MinionTypes;
import com.artillexstudios.axminions.minions.MinionWorldCache;
import com.artillexstudios.axminions.minions.skins.Skin;
import com.artillexstudios.axminions.utils.LocationUtils;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public final class AxMinionsCommand {

    public static void register() {
        new CommandTree("axminions")
                .withAliases("minion", "minions")
                .then(new LiteralArgument("version")
                        .withPermission("axminions.command.version")
                        .executes((sender, args) -> {
                            sender.sendMessage(StringUtils.formatToString(Language.PREFIX + "<green>You are running <white>AxMinions</white> version <white><version></white> on <white><implementation></white> version <white><implementation-version></white> (Implementing API version <white><api-version></white>)", Placeholder.unparsed("version", AxMinionsPlugin.getInstance().getDescription().getVersion()), Placeholder.unparsed("implementation", Bukkit.getName()), Placeholder.unparsed("implementation-version", Bukkit.getVersion()), Placeholder.unparsed("api-version", Bukkit.getBukkitVersion())));
                        })
                )
                .then(new LiteralArgument("debug")
                        .withPermission("axminions.command.debug")
                        .then(new LiteralArgument("spawn")
                                .withPermission("axminions.command.debug.spawn")
                                .executesPlayer((sender, args) -> {
                                    Location location = LocationUtils.toBlockCenter(sender.getLocation());
                                    MinionData data = new MinionData(0, MinionTypes.parse("miner"), new Level(1, 20, new Skin("cool", Map.ofEntries(Map.entry(EquipmentSlot.BOOTS, WrappedItemStack.wrap(new ItemStack(Material.DIAMOND_BOOTS))), Map.entry(EquipmentSlot.LEGGINGS, WrappedItemStack.wrap(new ItemStack(Material.DIAMOND_LEGGINGS))), Map.entry(EquipmentSlot.CHEST_PLATE, WrappedItemStack.wrap(new ItemStack(Material.DIAMOND_CHESTPLATE))), Map.entry(EquipmentSlot.HELMET, WrappedItemStack.wrap(new ItemStack(Material.DIAMOND_HELMET))), Map.entry(EquipmentSlot.MAIN_HAND, WrappedItemStack.wrap(new ItemStack(Material.DIAMOND_PICKAXE))), Map.entry(EquipmentSlot.OFF_HAND, WrappedItemStack.wrap(new ItemStack(Material.AIR)))))), new ItemStack(Material.DIAMOND_HOE), new Skin("cool", Map.ofEntries(Map.entry(EquipmentSlot.BOOTS, WrappedItemStack.wrap(new ItemStack(Material.DIAMOND_BOOTS))), Map.entry(EquipmentSlot.LEGGINGS, WrappedItemStack.wrap(new ItemStack(Material.DIAMOND_LEGGINGS))), Map.entry(EquipmentSlot.CHEST_PLATE, WrappedItemStack.wrap(new ItemStack(Material.DIAMOND_CHESTPLATE))), Map.entry(EquipmentSlot.HELMET, WrappedItemStack.wrap(new ItemStack(Material.DIAMOND_HELMET))), Map.entry(EquipmentSlot.MAIN_HAND, WrappedItemStack.wrap(new ItemStack(Material.DIAMOND_PICKAXE))), Map.entry(EquipmentSlot.OFF_HAND, WrappedItemStack.wrap(new ItemStack(Material.AIR))))), new HashMap<>());
                                    Minion minion = new Minion(location, data);
                                    minion.spawn();
                                    MinionArea area = MinionWorldCache.getArea(location.getWorld());
                                    area.load(minion);
                                    area.startTicking(location.getChunk());
                                })
                        )
                )
                .register();
    }
}
