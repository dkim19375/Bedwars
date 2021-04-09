package me.dkim19375.bedwars.plugin.enumclass

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.ChatColor
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.material.Colorable

enum class Team(val color: DyeColor, val displayName: String) {
    RED(DyeColor.RED, "Red"),
    YELLOW(DyeColor.YELLOW, "Yellow"),
    GREEN(DyeColor.LIME, "Lime"),
    BLUE(DyeColor.LIGHT_BLUE, "Blue");
}

fun Team.getColored(type: Material): ItemStack {
    return getColored(ItemStack(type))
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
    @Suppress("DEPRECATION", "LiftReturnOrAssignment") // fix warning = error?!
    return ItemStack(item.type, item.amount, color.data.toShort())
}

fun String.formatWithColors(color: DyeColor): String {
    val component = Component.text(this, TextColor.color(color.color.asRGB()))
    return LegacyComponentSerializer.legacyAmpersand().serialize(component) + ChatColor.RESET.toString()
}

fun DyeColor.formatText(text: String): String {
    return text.formatWithColors(this)
}