package com.artillexstudios.axminions.minions.actions.collectors.options.parser;

import com.artillexstudios.axapi.utils.Pair;
import com.artillexstudios.axminions.minions.actions.collectors.CollectorContext;
import com.artillexstudios.axminions.minions.actions.collectors.options.CollectorOption;
import redempt.crunch.CompiledExpression;
import redempt.crunch.Crunch;

import java.util.List;
import java.util.Map;

public final class CompiledExpressionOptionParser implements CollectorOptionParser {
    private final List<String> keys;
    private final CollectorOption<CompiledExpression> option;
    private final Pair<String, String>[] replacements;

    @SafeVarargs
    public CompiledExpressionOptionParser(List<String> keys, CollectorOption<CompiledExpression> option, Pair<String, String>... replacements) {
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

            builder.withOption(this.option, Crunch.compileExpression(value));
            break;
        }
    }
}
