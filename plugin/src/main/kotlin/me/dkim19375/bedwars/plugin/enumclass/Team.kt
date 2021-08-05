/*
 *     Bedwars, a minigame for spigot
 *     Copyright (C) 2021  dkim19375
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.dkim19375.bedwars.plugin.enumclass

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
                valueOf(str.uppercase())
            } catch (_: IllegalArgumentException) {
                return null
            }
        }
    }
}

@Suppress("unused")
fun Team.getColored(material: Material): ItemStack = getColored(ItemStack(material))

fun Team.getColored(item: ItemStack): ItemStack {
    val wrapper = item.getWrapper()
    return wrapper.toItemStack(color)
}