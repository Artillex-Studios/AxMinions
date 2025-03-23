package com.artillexstudios.axminions.minions.actions.collectors.options.parser;

import com.artillexstudios.axapi.utils.Pair;
import com.artillexstudios.axminions.minions.actions.collectors.CollectorContext;
import com.artillexstudios.axminions.minions.actions.collectors.options.CollectorOption;

import java.util.List;
import java.util.Map;

public final class StringOptionParser implements CollectorOptionParser {
    private final List<String> keys;
    private final CollectorOption<String> option;
    private final Pair<String, String>[] replacements;

    @SafeVarargs
    public StringOptionParser(List<String> keys, CollectorOption<String> option, Pair<String, String>... replacements) {
        this.keys = keys;
        this.option = option;
        this.replacements = replacements;
    }

    @Override
    public void parse(Map<Object, Object> config, CollectorContext.Builder builder) {
        for (String key : this.keys) {
            String value = (String) config.get(key);
            if (value == null) {
                continue;
            }

            for (Pair<String, String> replacement : this.replacements) {
                value = value.replace(replacement.first(), replacement.second());
            }

            builder.withOption(this.option, value);
            break;
        }
    }
}
