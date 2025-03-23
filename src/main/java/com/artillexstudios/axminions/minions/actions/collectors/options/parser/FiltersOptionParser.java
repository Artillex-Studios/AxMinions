package com.artillexstudios.axminions.minions.actions.collectors.options.parser;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axminions.minions.actions.collectors.CollectorContext;
import com.artillexstudios.axminions.minions.actions.collectors.CollectorRegistry;
import com.artillexstudios.axminions.minions.actions.collectors.options.CollectorOptions;
import com.artillexstudios.axminions.minions.actions.filters.Filter;
import com.artillexstudios.axminions.minions.actions.filters.Filters;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.Map;

public final class FiltersOptionParser implements CollectorOptionParser {

    @Override
    public void parse(Map<Object, Object> config, CollectorContext.Builder builder) {
        List<Map<Object, Object>> filterMap = (List<Map<Object, Object>>) config.get("filters");
        if (filterMap == null) {
            builder.withOption(CollectorOptions.FILTERS, List.of());
            return;
        }

        List<Filter<?>> filters = new ObjectArrayList<>(1);
        for (Map<Object, Object> map : filterMap) {
            String filterId = (String) map.get("id");

            if (filterId == null) {
                LogUtils.warn("Could not find id in filter config!");
                continue;
            }

            Filter<?> filter = Filters.parse(filterId, map);
            if (filter == null) {
                LogUtils.warn("Could not find filter with id {}!", filterId);
                continue;
            }

            String collectorID = builder.option(CollectorOptions.COLLECTOR_ID);
            List<Class<?>> inputClasses = filter.inputClasses();
            Class<?> collectedClass = CollectorRegistry.getCollectedClass(collectorID);
            if (!inputClasses.contains(collectedClass)) {
                LogUtils.error("Could not apply filter with id {} to collector {} due to mismatching input! Filter input: {}, Collector output: {}.", filterId, collectorID, String.join(", ", inputClasses.stream().map(Class::getName).toList()), collectedClass.getName());
                continue;
            }

            filters.add(filter);
        }

        builder.withOption(CollectorOptions.FILTERS, filters);
    }
}

