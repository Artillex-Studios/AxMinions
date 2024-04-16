package com.artillexstudios.axminions.minions.miniontype

import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import com.artillexstudios.axminions.api.warnings.Warnings
import com.artillexstudios.axminions.minions.MinionTicker
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe

class CrafterMinionType : MinionType("collector", AxMinionsPlugin.INSTANCE.getResource("minions/crafter.yml")!!) {

    override fun shouldRun(minion: Minion): Boolean {
        return MinionTicker.getTick() % minion.getNextAction() == 0L
    }

    override fun onToolDirty(minion: Minion) {
        val minionImpl = minion as com.artillexstudios.axminions.minions.Minion
        minionImpl.setRange(1.0)
        minionImpl.setNextAction(getLong("speed", minion.getLevel()).toInt())
    }

    override fun run(minion: Minion) {
        if (minion.getLinkedChest() == null) {
            Warnings.NO_CONTAINER.display(minion)
            return
        }

        val type = minion.getLinkedChest()!!.block.type
        if (type != Material.CHEST && type != Material.TRAPPED_CHEST && type != Material.BARREL) {
            Warnings.NO_CONTAINER.display(minion)
            minion.setLinkedChest(null)
            return
        }

        if (minion.getLinkedInventory() == null) {
            Warnings.NO_CONTAINER.display(minion)
            minion.setLinkedChest(null)
            return
        }

        Warnings.remove(minion, Warnings.NO_CONTAINER)

        if (!minion.canUseTool()) {
            Warnings.NO_TOOL.display(minion)
            return
        }

        Warnings.remove(minion, Warnings.NO_TOOL)

        val tool = minion.getTool()

        val recipes = Bukkit.getRecipesFor(tool ?: return)

        val shaped = arrayListOf<ShapedRecipe>()
        val shapeless = arrayListOf<ShapelessRecipe>()

        for (recipe in recipes) {
            println("RECIPE!")
            if (recipe is ShapedRecipe) {
                println("SHAPED!")
                shaped.add(recipe)
            } else if (recipe is ShapelessRecipe) {
                shapeless.add(recipe)
            }
        }

        val inv = minion.getLinkedInventory() ?: return
        val items = inv.contents

        val contents = HashMap<ItemStack, Int>()

        a@ for (item in items) {
            if (item == null || item.type.isAir) continue

            if (contents.isEmpty()) {
                contents[item] = item.amount
                continue
            }

            val iterator: Iterator<MutableMap.MutableEntry<ItemStack, Int>> = contents.entries.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()

                if (item.isSimilar(next.key)) {
                    next.setValue(next.value + item.amount)
                    continue@a
                }
            }

            contents[item] = item.amount
        }

        // SHAPELESS
        var recipeIterator = shapeless.iterator()
        while (recipeIterator.hasNext()) {
            val recipe = recipeIterator.next()

            if (!canCraftShapeless(recipe, contents)) {
                recipeIterator.remove()
                continue
            }

            doCraftShapeless(inv, recipe, contents)

            recipeIterator = shapeless.iterator()
        }


        // SHAPED
        var shapedIterator = shaped.iterator()
        while (shapedIterator.hasNext()) {
            val recipe = shapedIterator.next()

            if (!canCraftShaped(recipe, contents)) {
                shapedIterator.remove()
                continue
            }

            doCraftShaped(inv, recipe, contents)

            shapedIterator = shaped.iterator()
        }
    }

    private fun canCraftShapeless(recipe: ShapelessRecipe, contents: HashMap<ItemStack, Int>): Boolean {
        val clone = contents.clone() as HashMap<ItemStack, Int>
        for (recipeChoice in recipe.choiceList) {
            var hasEnough = false

            val iterator = clone.entries.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()

                if (next.key.isSimilar(recipeChoice.itemStack) && next.value >= recipeChoice.itemStack.amount) {
                    hasEnough = true
                    val amount = next.value - recipeChoice.itemStack.amount
                    if (amount == 0) {
                        iterator.remove()
                    } else {
                        next.setValue(amount)
                    }
                    break
                }
            }

            if (!hasEnough) {
                return false
            }
        }

        return true
    }

    private fun canCraftShaped(recipe: ShapedRecipe, contents: HashMap<ItemStack, Int>): Boolean {
        val clone = contents.clone() as HashMap<ItemStack, Int>
        for (recipeChoice in recipe.choiceMap.values) {
            var hasEnough = false

            val iterator = clone.entries.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()

                if (next.key.isSimilar(recipeChoice.itemStack) && next.value >= recipeChoice.itemStack.amount) {
                    hasEnough = true
                    val amount = next.value - recipeChoice.itemStack.amount
                    if (amount == 0) {
                        iterator.remove()
                    } else {
                        next.setValue(amount)
                    }
                    break
                }
            }

            if (!hasEnough) {
                return false
            }
        }

        return true
    }

    private fun doCraftShapeless(inventory: Inventory, recipe: ShapelessRecipe, contents: HashMap<ItemStack, Int>) {
        for (recipeChoice in recipe.choiceList) {
            val item = recipeChoice.itemStack.clone()

            inventory.removeItem(item)

            val iterator = contents.entries.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()

                if (item.isSimilar(next.key)) {
                    val amount = next.value - item.amount
                    if (amount == 0) {
                        iterator.remove()
                    } else {
                        next.setValue(amount)
                    }
                    break
                }
            }
        }

        val result = recipe.result.clone()
        inventory.addItem(result)
        val iterator: Iterator<MutableMap.MutableEntry<ItemStack, Int>> = contents.entries.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()

            if (result.isSimilar(next.key)) {
                next.setValue(next.value + result.amount)
                return
            }
        }

        contents[result] = result.amount
    }

    private fun doCraftShaped(inventory: Inventory, recipe: ShapedRecipe, contents: HashMap<ItemStack, Int>) {
        for (recipeChoice in recipe.choiceMap.values) {
            val item = recipeChoice.itemStack.clone()

            inventory.removeItem(item)

            val iterator = contents.entries.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()

                if (item.isSimilar(next.key)) {
                    val amount = next.value - item.amount
                    if (amount == 0) {
                        iterator.remove()
                    } else {
                        next.setValue(amount)
                    }
                    break
                }
            }
        }

        val result = recipe.result.clone()
        inventory.addItem(result)
        val iterator: Iterator<MutableMap.MutableEntry<ItemStack, Int>> = contents.entries.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()

            if (result.isSimilar(next.key)) {
                next.setValue(next.value + result.amount)
                return
            }
        }

        contents[result] = result.amount
    }
}