package com.artillexstudios.axminions.minions.actions.collectors.implementation;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.collectors.Collector;
import com.artillexstudios.axminions.minions.actions.collectors.CollectorContext;
import com.artillexstudios.axminions.minions.actions.collectors.CollectorOptionNotPresentException;
import com.artillexstudios.axminions.minions.actions.collectors.options.CollectorOptions;
import com.artillexstudios.axminions.minions.actions.requirements.Requirement;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import redempt.crunch.CompiledExpression;

import java.util.List;
import java.util.function.Consumer;

public final class EntityCollector extends Collector<Entity> {

    public EntityCollector(CollectorContext context, List<Requirement> requirements) {
        super(context, requirements);
    }

    @Override
    public Class<?> getCollectedClass() {
        return Entity.class;
    }

    @Override
    public void collect(Minion minion, Consumer<Entity> consumer) {
        CompiledExpression limitExpression = this.context.optionOrDefault(CollectorOptions.LIMIT_RAW, Collector.ZERO_EXPRESSION);
        CompiledExpression rangeExpression = this.context.option(CollectorOptions.RANGE_RAW);

        try {
            this.context.option(CollectorOptions.SHAPE).getBlocks(this.context.toBuilder()
                    .withOption(CollectorOptions.LIMIT, limitExpression.getVariableCount() == 0 ? (int) limitExpression.evaluate() : (int) limitExpression.evaluate(minion.level().id()))
                    .withOption(CollectorOptions.RANGE, rangeExpression.getVariableCount() == 0 ? rangeExpression.evaluate() : rangeExpression.evaluate(minion.level().id()))
                    .withOption(CollectorOptions.LOCATION, minion.location())
                    .withOption(CollectorOptions.FACING, minion.facing().blockFace())
                    .withOption(CollectorOptions.ENTITY_CONSUMER, consumer)
                    .build()
            );
        } catch (CollectorOptionNotPresentException exception) {
            LogUtils.warn("Failed to tick minion due to collector option {} not being present! This is not a configuration issue, but an issue in the code! Report to the developer!", exception.option());
        }
    }
}
