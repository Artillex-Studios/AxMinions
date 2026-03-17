package com.artillexstudios.axminions.minions.actions.collectors.options.parser;

import com.artillexstudios.axapi.config.adapters.MapConfigurationGetter;
import com.artillexstudios.axminions.minions.actions.collectors.CollectorContext;
import com.artillexstudios.axminions.minions.actions.collectors.options.parser.exception.InvalidCollectorOptionException;

public interface CollectorOptionParser {

    void parse(MapConfigurationGetter config, CollectorContext.Builder builder) throws InvalidCollectorOptionException;
}
