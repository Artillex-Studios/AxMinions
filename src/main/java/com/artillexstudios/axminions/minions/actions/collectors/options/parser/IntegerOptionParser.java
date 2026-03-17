package com.artillexstudios.axminions.minions.actions.collectors.options.parser;

import com.artillexstudios.axapi.config.adapters.MapConfigurationGetter;
import com.artillexstudios.axminions.minions.actions.collectors.CollectorContext;
import com.artillexstudios.axminions.minions.actions.collectors.options.CollectorOption;

import java.util.List;

public final class IntegerOptionParser implements CollectorOptionParser {
    private final List<String> keys;
    private final CollectorOption<Integer> option;

    public IntegerOptionParser(List<String> keys, CollectorOption<Integer> option) {
        this.keys = keys;
        this.option = option;
    }

    @Override
    public void parse(MapConfigurationGetter config, CollectorContext.Builder builder) {
        for (String key : this.keys) {
            Integer value = config.getInteger(key);
            if (value == null) {
                continue;
            }

            builder.withOption(this.option, value);
            break;
        }
    }
}
