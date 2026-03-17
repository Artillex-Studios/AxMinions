package com.artillexstudios.axminions.command.arguments;

public final class MinionTypeArgument {

//    public static Argument<MinionType> minionType(String minionType) {
//        return new CustomArgument<>(new StringArgument(minionType), info -> {
//            MinionType type = MinionTypes.parse(info.input());
//
//            if (type == null) {
//                throw CustomArgument.CustomArgumentException.fromAdventureComponent(StringUtils.format(Language.PREFIX + Language.ERROR_TYPE_NOT_FOUND, Placeholder.parsed("name", info.input())));
//            }
//
//            return type;
//        }).replaceSuggestions(ArgumentSuggestions.strings(info -> {
//            return MinionTypes.types().toArray(new String[0]);
//        }));
//    }
}
