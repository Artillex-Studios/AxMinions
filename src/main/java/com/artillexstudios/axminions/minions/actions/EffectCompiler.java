package com.artillexstudios.axminions.minions.actions;

import com.artillexstudios.axapi.config.adapters.MapConfigurationGetter;
import com.artillexstudios.axminions.minions.actions.collectors.Collector;
import com.artillexstudios.axminions.minions.actions.effects.Effect;

import java.util.List;

public interface EffectCompiler {

    List<Effect<Object, Object>> compile(Collector<?> collector, Effect<Object, Object> parent, List<MapConfigurationGetter> effects);
}
