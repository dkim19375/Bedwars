package me.dkim19375.bedwars.plugin.enumclass

import me.dkim19375.bedwars.plugin.data.Potion
import me.dkim19375.bedwars.plugin.util.ItemWrapper
import me.dkim19375.bedwars.plugin.util.getWrapper
import org.bukkit.ChatColor
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

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

fun Team.getColored(material: Material): ItemStack = getColored(ItemStack(material))

fun Team.getColored(item: ItemStack): ItemStack {
    val wrapper = item.getWrapper()
    return wrapper.toItemStack(color)
}