package com.artillexstudios.axminions.command.arguments;

public final class MinionLevelArgument {

//    public static Argument<Level> level(String level) {
//        return new CustomArgument<>(new StringArgument(level), info -> {
//            MinionType type = info.previousArgs().getByClass("miniontype", MinionType.class);
//
//            if (type == null) {
//                throw CustomArgument.CustomArgumentException.fromAdventureComponent(StringUtils.format(Language.PREFIX + Language.ERROR_TYPE_NOT_FOUND, Placeholder.parsed("name", info.input())));
//            }
//
//            String argument = info.input();
//            int levelInt;
//            try {
//                levelInt = Integer.parseInt(argument);
//            } catch (NumberFormatException exception) {
//                throw CustomArgument.CustomArgumentException.fromAdventureComponent(StringUtils.format(Language.PREFIX + Language.ERROR_INVALID_NUMBER, Placeholder.parsed("number", info.input())));
//            }
//
//            Level typeLevel = type.level(levelInt);
//            if (typeLevel == null) {
//                throw CustomArgument.CustomArgumentException.fromAdventureComponent(StringUtils.format(Language.PREFIX + Language.ERROR_INVALID_LEVEL, Placeholder.parsed("level", info.input())));
//            }
//
//            return typeLevel;
//        }).replaceSuggestions(ArgumentSuggestions.strings(info -> {
//            MinionType type = info.previousArgs().getByClass("miniontype", MinionType.class);
//            if (type == null) {
//                return new String[0];
//            }
//
//            return type.levels()
//                    .keySet()
//                    .intStream()
//                    .mapToObj(Integer::toString)
//                    .toList()
//                    .toArray(new String[0]);
//        }));
//    }
}
