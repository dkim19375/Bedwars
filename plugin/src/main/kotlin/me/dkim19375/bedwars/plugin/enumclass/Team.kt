package me.dkim19375.bedwars.plugin.enumclass

import org.bukkit.ChatColor
import org.bukkit.DyeColor
import org.bukkit.inventory.ItemStack
import org.bukkit.material.Colorable

enum class Team(val color: DyeColor, val chatColor: ChatColor, val displayName: String) {
    RED(DyeColor.RED, ChatColor.RED, "Red"),
    YELLOW(DyeColor.YELLOW, ChatColor.YELLOW, "Yellow"),
    GREEN(DyeColor.LIME, ChatColor.GREEN, "Green"),
    BLUE(DyeColor.LIGHT_BLUE, ChatColor.BLUE, "Blue");

    companion object {
        fun fromString(str: String?): Team? {
            str ?: return null
            return try {
                valueOf(str.toUpperCase())
            } catch (_: IllegalArgumentException) {
                return null
            }
        }
    }
}

fun Team.getColored(item: ItemStack): ItemStack {
    if (item.clone().itemMeta is Colorable) {
        val newItem = item.clone()
        newItem.itemMeta?.let {
            (it as Colorable).color = color
            newItem.itemMeta = it
        }
        return newItem
    }
    @Suppress("DEPRECATION")
    return ItemStack(item.type, item.amount, color.data.toShort())
}