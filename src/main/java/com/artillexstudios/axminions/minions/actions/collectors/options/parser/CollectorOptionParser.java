package com.artillexstudios.axminions.minions.actions.collectors.options.parser;

import com.artillexstudios.axminions.minions.actions.collectors.CollectorContext;
import com.artillexstudios.axminions.minions.actions.collectors.options.parser.exception.InvalidCollectorOptionException;

import java.util.Map;

public interface CollectorOptionParser {

    void parse(Map<Object, Object> config, CollectorContext.Builder builder) throws InvalidCollectorOptionException;
}
