package com.artillexstudios.axminions.utils

import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment

object Enchantments {
    val EFFICIENCY: Enchantment? = Enchantment.getByKey(NamespacedKey.minecraft("efficiency"))
    val UNBREAKING: Enchantment? = Enchantment.getByKey(NamespacedKey.minecraft("unbreaking"))
}

