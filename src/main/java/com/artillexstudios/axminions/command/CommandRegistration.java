package com.artillexstudios.axminions.command;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axminions.minions.Level;
import com.artillexstudios.axminions.minions.MinionType;
import com.artillexstudios.axminions.minions.MinionTypes;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.exception.CommandErrorException;

public enum CommandRegistration {
    INSTANCE;
    private BukkitCommandHandler handler;

    public void register(JavaPlugin plugin) {
        this.handler = BukkitCommandHandler.create(plugin);

        this.handler.registerValueResolver(MinionType.class, ctx -> {
            MinionType parse = MinionTypes.parse(ctx.popForParameter());
            if (parse == null) {
                // TODO: Localize
                throw new CommandErrorException("Couldn't find MinionType named " + ctx.popForParameter() + "!");
            }

            return parse;
        });

        this.handler.registerValueResolver(Level.class, ctx -> {
            MinionType type = ctx.getResolvedArgument(MinionType.class);
            String argument = ctx.popForParameter();
            int levelInt;
            try {
                levelInt = Integer.parseInt(argument);
            } catch (NumberFormatException exception) {
                throw new CommandErrorException();
            }

            Level typeLevel = type.level(levelInt);
            if (typeLevel == null) {
                throw new CommandErrorException();
            }

            return typeLevel;
        });

        this.handler.getAutoCompleter().registerParameterSuggestions(MinionType.class, (_, _, _) -> {
            return MinionTypes.types();
        });

        this.handler.getAutoCompleter().registerParameterSuggestions(Level.class, (args, _, command) -> {
            int i = 0;
            for (CommandParameter parameter : command.getParameters()) {
                Class<?> type = parameter.getJavaParameter().getType();
                if (type == MinionType.class) {
                    String minionType = args.get(i + 1);
                    LogUtils.debug(minionType);
                    return MinionTypes.parse(minionType).levels()
                            .keySet()
                            .intStream()
                            .mapToObj(Integer::toString)
                            .toList();
                }
                i++;
            }

            throw new CommandErrorException("An error occurred!");
        });

        this.handler.register(new AxMinionsCommand());
        this.handler.registerBrigadier();
    }
}
