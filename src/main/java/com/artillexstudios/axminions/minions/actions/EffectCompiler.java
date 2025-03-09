package com.artillexstudios.axminions.minions.actions;

import com.artillexstudios.axminions.minions.actions.collectors.Collector;
import com.artillexstudios.axminions.minions.actions.effects.Effect;

import java.util.List;
import java.util.Map;

public interface EffectCompiler {

    List<Effect<Object, Object>> compile(Collector<?> collector, Effect<Object, Object> parent, List<Map<Object, Object>> effects);
}
