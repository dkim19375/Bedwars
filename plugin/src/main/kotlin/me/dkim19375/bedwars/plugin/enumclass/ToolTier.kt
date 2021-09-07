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

import me.dkim19375.bedwars.plugin.util.getAllContents
import me.dkim19375.bedwars.plugin.util.isTool
import me.dkim19375.bedwars.plugin.util.isWeapon
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

enum class ToolTier(val pickaxe: Material, val axe: Material, val sword: Material, val tier: Int) {
    WOOD(Material.WOOD_PICKAXE, Material.WOOD_AXE, Material.WOOD_SWORD, 1),
    STONE(Material.STONE_PICKAXE, Material.STONE_AXE, Material.STONE_SWORD, 2),
    IRON(Material.IRON_PICKAXE, Material.IRON_AXE, Material.IRON_SWORD, 3),
    DIAMOND(Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE, Material.DIAMOND_SWORD, 4);
}

fun Material.getToolTier(): Pair<ToolTier, ToolType>? {
    for (tier in ToolTier.values()) {
        if (tier.pickaxe == this) {
            return tier to ToolType.PICKAXE
        }
        if (tier.axe == this) {
            return tier to ToolType.AXE
        }
        if (tier.sword == this) {
            return tier to ToolType.SWORD
        }
    }
    return null
}

fun PlayerInventory.getToolTier(): Map<ToolType, ToolTier> {
    val map = mutableMapOf<ToolType, ToolTier>()
    for (item in getAllContents().filterNotNull().map(ItemStack::getType)
        .filter { it.isTool() || it.isWeapon() }) {
        val newTier = item.getToolTier() ?: continue
        val pickaxe = map[ToolType.PICKAXE]
        val axe = map[ToolType.AXE]
        val sword = map[ToolType.SWORD]
        when (newTier.second) {
            ToolType.PICKAXE -> if (pickaxe == null || pickaxe.tier < newTier.first.tier) {
                map[ToolType.PICKAXE] = newTier.first
            }
            ToolType.AXE -> if (axe == null || axe.tier < newTier.first.tier) {
                map[ToolType.AXE] = newTier.first
            }
            ToolType.SWORD -> if (sword == null || sword.tier < newTier.first.tier) {
                map[ToolType.SWORD] = newTier.first
            }
        }
    }
    return map
}