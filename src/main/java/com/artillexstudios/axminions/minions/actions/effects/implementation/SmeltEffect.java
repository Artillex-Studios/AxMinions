package com.artillexstudios.axminions.minions.actions.effects.implementation;

import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.items.component.DataComponents;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.effects.Effect;
import com.artillexstudios.axminions.utils.ItemCollection;
import com.artillexstudios.axminions.utils.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.Iterator;
import java.util.Map;

public class SmeltEffect extends Effect<ItemCollection, ItemCollection> {
    private static final Object2ObjectLinkedOpenHashMap<Material, Material> recipes = new Object2ObjectLinkedOpenHashMap<>();

    static {
        Iterator<Recipe> recipeIterator = Bukkit.recipeIterator();
        while (recipeIterator.hasNext()) {
            Recipe next = recipeIterator.next();
            if (!(next instanceof FurnaceRecipe furnaceRecipe)) {
                continue;
            }

            recipes.put(furnaceRecipe.getInput().getType(), furnaceRecipe.getResult().getType());
        }
    }

    public SmeltEffect(Map<Object, Object> configuration) {
        super(configuration);
    }

    @Override
    public ItemCollection run(Minion minion, ItemCollection argument) {
        for (ItemStack itemStack : argument) {
            Material material = itemStack.getType();
            Material to = recipes.get(material);
            if (to == null) {
                continue;
            }

            // TODO: Smelt event
            WrappedItemStack.edit(itemStack, item -> {
                item.set(DataComponents.material(), to);
                return null;
            });
        }

        return argument;
    }

    @Override
    public Class<?> inputClass() {
        return ItemCollection.class;
    }

    @Override
    public Class<?> outputClass() {
        return ItemCollection.class;
    }
}
