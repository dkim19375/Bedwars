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
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

enum class ToolTier(val pickaxe: Material, val axe: Material, val tier: Int) {
    WOOD(Material.WOOD_PICKAXE, Material.WOOD_AXE, 1),
    STONE(Material.STONE_PICKAXE, Material.STONE_AXE, 2),
    IRON(Material.IRON_PICKAXE, Material.IRON_AXE, 3),
    DIAMOND(Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE, 4);
}

fun Material.getToolTier(): Pair<ToolTier, Boolean>? {
    for (tier in ToolTier.values()) {
        if (tier.pickaxe == this) {
            return tier to true
        }
        if (tier.axe == this) {
            return tier to false
        }
    }
    return null
}

fun PlayerInventory.getToolTier(): Pair<ToolTier?, ToolTier?> {
    var pickaxe: ToolTier? = null
    var axe: ToolTier? = null
    for (item in getAllContents().filterNotNull().map(ItemStack::getType).filter(Material::isTool)) {
        val newTier = item.getToolTier() ?: continue
        if (newTier.second) {
            if (pickaxe == null || pickaxe.tier < newTier.first.tier) {
                pickaxe = newTier.first
            }
            continue
        }
        if (axe == null || axe.tier < newTier.first.tier) {
            axe = newTier.first
        }
    }
    return pickaxe to axe
}