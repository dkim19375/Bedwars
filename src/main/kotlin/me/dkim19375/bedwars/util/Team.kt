package me.dkim19375.bedwars.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.ChatColor
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.material.Colorable
import org.bukkit.material.MaterialData

enum class Team(val color: DyeColor, val displayName: String) {
    RED(DyeColor.RED, "Red"),
    YELLOW(DyeColor.YELLOW, "Yellow"),
    GREEN(DyeColor.LIME, "Lime"),
    BLUE(DyeColor.LIGHT_BLUE, "Blue");
}

fun Team.getColored(type: Material): ItemStack {
    val item = ItemStack(type)
    if (item.data !is Colorable) {
        return item
    }
    val woolData = item.data as Colorable
    woolData.color = color
    item.data = woolData as MaterialData
    return item
}

fun String.formatWithColors(color: DyeColor): String {
    val component = Component.text(this, TextColor.color(color.color.asRGB()))
    return LegacyComponentSerializer.legacyAmpersand().serialize(component) + ChatColor.RESET.toString()
}