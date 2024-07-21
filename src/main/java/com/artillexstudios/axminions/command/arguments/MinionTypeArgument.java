package com.artillexstudios.axminions.command.arguments;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axminions.config.Language;
import com.artillexstudios.axminions.minions.MinionType;
import com.artillexstudios.axminions.minions.MinionTypes;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public final class MinionTypeArgument {

    public static Argument<MinionType> minionType(String minionType) {
        return new CustomArgument<>(new StringArgument(minionType), info -> {
            MinionType type = MinionTypes.parse(info.input());

            if (type == null) {
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(StringUtils.format(Language.PREFIX + Language.ERROR_TYPE_NOT_FOUND, Placeholder.parsed("name", info.input())));
            }

            return type;
        }).replaceSuggestions(ArgumentSuggestions.strings(info -> {
            return MinionTypes.types().toArray(new String[0]);
        }));
    }
}
