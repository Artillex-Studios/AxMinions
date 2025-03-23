package com.artillexstudios.axminions.command;

import com.artillexstudios.axapi.gui.SignInput;
import com.artillexstudios.axapi.utils.ContainerUtils;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axapi.utils.MessageUtils;
import com.artillexstudios.axminions.AxMinionsPlugin;
import com.artillexstudios.axminions.command.arguments.MinionLevelArgument;
import com.artillexstudios.axminions.command.arguments.MinionTypeArgument;
import com.artillexstudios.axminions.config.Config;
import com.artillexstudios.axminions.config.Language;
import com.artillexstudios.axminions.config.Minions;
import com.artillexstudios.axminions.config.Skins;
import com.artillexstudios.axminions.minions.Level;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.MinionArea;
import com.artillexstudios.axminions.minions.MinionData;
import com.artillexstudios.axminions.minions.MinionType;
import com.artillexstudios.axminions.minions.MinionWorldCache;
import com.artillexstudios.axminions.utils.Direction;
import com.artillexstudios.axminions.utils.FileUtils;
import com.artillexstudios.axminions.utils.LocationUtils;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public final class AxMinionsCommand {

    public static void register() {
        new CommandTree("axminions")
                .withAliases("minion", "minions")
                .then(new LiteralArgument("give")
                        .withPermission("axminions.command.give")
                        .then(new PlayerArgument("player")
                                .then(MinionTypeArgument.minionType("miniontype")
                                        .then(MinionLevelArgument.level("level")
                                                .then(new IntegerArgument("amount")
                                                        .executes((sender, args) -> {
                                                            Player player = args.getByClass("player", Player.class);
                                                            MinionType type = args.getByClass("miniontype", MinionType.class);
                                                            Level level = args.getByClass("level", Level.class);
                                                            Integer amount = args.getByClass("amount", Integer.class);
                                                            handleGive(sender, player, type, level, amount);
                                                        })
                                                )
                                        )
                                )
                        )
                )
                .then(new LiteralArgument("version")
                        .withPermission("axminions.command.version")
                        .executes((sender, args) -> {
                            MessageUtils.sendMessage(sender, Language.PREFIX, "<green>You are running <white>AxMinions</white> version <white><version></white> on <white><implementation></white> version <white><implementation-version></white> (Implementing API version <white><api-version></white>)",
                                    Placeholder.unparsed("version", AxMinionsPlugin.instance().getDescription().getVersion()),
                                    Placeholder.unparsed("implementation", Bukkit.getName()),
                                    Placeholder.unparsed("implementation-version", Bukkit.getVersion()),
                                    Placeholder.unparsed("api-version", Bukkit.getBukkitVersion())
                            );
                        })
                )
                .then(new LiteralArgument("reload")
                        .withPermission("axminions.command.reload")
                        .executes((sender, args) -> {
                            long start = System.nanoTime();
                            List<File> failed = new ArrayList<>();

                            for (World world : Bukkit.getWorlds()) {
                                MinionWorldCache.clear(world);
                            }

                            if (!Config.reload()) {
                                failed.add(FileUtils.PLUGIN_DIRECTORY.resolve("config.yml").toFile());
                            }

                            if (!Language.reload()) {
                                failed.add(FileUtils.PLUGIN_DIRECTORY.resolve("language").resolve(Language.lastLanguage + ".yml").toFile());
                            }

                            if (!Skins.reload()) {
                                failed.add(FileUtils.PLUGIN_DIRECTORY.resolve("skins.yml").toFile());
                            }

                            Minions.reload();
                            failed.addAll(Minions.failedToLoad());

                            CompletableFuture<?>[] futures = Minions.loadingMinions().toArray(new CompletableFuture[0]);
                            List<World> worlds = Bukkit.getWorlds();
                            AtomicInteger counter = new AtomicInteger();
                            CompletableFuture.allOf(futures).thenRun(() -> {
                                Minions.loadingMinions().clear();

                                for (World world : worlds) {
                                    AxMinionsPlugin.instance().handler().loadMinions(world).toCompletableFuture().thenAccept(loaded -> {
                                        if (Config.debug) {
                                            LogUtils.debug("Loaded {} minions in world {} in {} ms!", loaded.firstInt(), world.getName(), loaded.secondLong() / 1_000_000);
                                        }
                                        if (counter.incrementAndGet() != futures.length * worlds.size()) {
                                            return;
                                        }

                                        if (failed.isEmpty()) {
                                            MessageUtils.sendMessage(sender, Language.PREFIX, Language.RELOAD_SUCCESS, Placeholder.parsed("time", Long.toString((System.nanoTime() - start) / 1_000_000)));
                                        } else {
                                            MessageUtils.sendMessage(sender, Language.PREFIX, Language.RELOAD_FAIL, Placeholder.parsed("time", Long.toString((System.nanoTime() - start) / 1_000_000)), Placeholder.parsed("files", String.join(", ", failed.stream()
                                                    .map(File::getName)
                                                    .toList())
                                            ));
                                        }
                                    });
                                }
                            });
                        })
                )
                .then(new LiteralArgument("debug")
                        .withPermission("axminions.command.debug")
                        .then(new LiteralArgument("spawn")
                                .withPermission("axminions.command.debug.spawn")
                                .then(MinionTypeArgument.minionType("miniontype")
                                        .then(MinionLevelArgument.level("level")
                                                .executesPlayer((sender, args) -> {
                                                    MinionType type = args.getByClass("miniontype", MinionType.class);
                                                    Level level = args.getByClass("level", Level.class);
                                                    Location location = LocationUtils.toBlockCenter(sender.getLocation());
                                                    MinionData data = new MinionData(0, type, Direction.NORTH, null, level, 0, new ItemStack(Material.DIAMOND_PICKAXE), null, new HashMap<>());
                                                    Minion minion = new Minion(location, data);
                                                    minion.spawn();
                                                    MinionArea area = MinionWorldCache.getArea(location.getWorld());
                                                    MinionWorldCache.add(minion);
                                                    area.startTicking(location.getChunk());
                                                })
                                        )
                                )
                        )
                        .then(new LiteralArgument("signinput")
                                .executesPlayer((sender, args) -> {
                                    SignInput signInput = new SignInput.Builder()
                                            .setHandler((player, response) -> {
                                                for (Component component : response) {
                                                    player.sendMessage(StringUtils.formatToString(MiniMessage.miniMessage().serialize(component)));
                                                }
                                            })
                                            .build(sender);
                                    signInput.open();
                                })
                        )
//                        .then(new LiteralArgument("incomingpackets")
//                                .executes((sender, args) -> {
//                                    AxMinionsPlugin.instance().flags().DEBUG_INCOMING_PACKETS.set(!AxMinionsPlugin.instance().flags().DEBUG_INCOMING_PACKETS.get());
//                                })
//                        )
//                        .then(new LiteralArgument("outgoingpackets")
//                                .executes((sender, args) -> {
//                                    AxMinionsPlugin.instance().flags().DEBUG_OUTGOING_PACKETS.set(!AxMinionsPlugin.instance().flags().DEBUG_OUTGOING_PACKETS.get());
//                                })
//                        )
                )
                .register();
    }

    private static void handleGive(CommandSender sender, Player player, MinionType type, Level level, Integer amount) {
        ItemStack item = type.item(new MinionData(0, type, null, null, level, 0, null, null, new HashMap<>(0)));
        item.setAmount(amount);
        ContainerUtils.INSTANCE.addOrDrop(player.getInventory(), List.of(item), player.getLocation());
    }
}
