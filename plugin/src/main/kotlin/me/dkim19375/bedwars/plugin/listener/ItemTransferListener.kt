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

package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.util.isArmor
import me.dkim19375.bedwars.plugin.util.isTool
import me.dkim19375.bedwars.plugin.util.isWeapon
import me.dkim19375.itemmovedetectionlib.event.InventoryItemTransferEvent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

class ItemTransferListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler(ignoreCancelled = true)
    private fun InventoryItemTransferEvent.onTransfer() {
        plugin.gameManager.getGame(player) ?: return
        checkSpecialItems()
        checkCraftingMenu()
    }

    private fun InventoryItemTransferEvent.checkCraftingMenu() {
        if (!type.isToCrafting) {
            return
        }
        Bukkit.broadcastMessage(
            "Cancelled for crafting: ${
                items.map(ItemStack::getType).joinToString(transform = Material::name)
            }"
        )
        isCancelled = true
    }

    private fun InventoryItemTransferEvent.checkSpecialItems() {
        if (!type.isFromSelf) {
            return
        }
        val materials = items.map(ItemStack::getType)
        if (materials.contains(Material.COMPASS)) {
            isCancelled = true
            Bukkit.broadcastMessage(
                "Cancelled for compass: ${
                    items.map(ItemStack::getType).joinToString(transform = Material::name)
                }"
            )
            return
        }
        if (materials.any(Material::isTool)) {
            isCancelled = true
            Bukkit.broadcastMessage(
                "Cancelled for tool: ${
                    items.map(ItemStack::getType).joinToString(transform = Material::name)
                }"
            )
            return
        }
        if (materials.any(Material::isWeapon)) {
            isCancelled = true
            Bukkit.broadcastMessage(
                "Cancelled for weapon: ${
                    items.map(ItemStack::getType).joinToString(transform = Material::name)
                }"
            )
            return
        }
        if (materials.any(Material::isArmor)) {
            isCancelled = true
            Bukkit.broadcastMessage(
                "Cancelled for armor: ${
                    items.map(ItemStack::getType).joinToString(transform = Material::name)
                }"
            )
            return
        }
    }
}