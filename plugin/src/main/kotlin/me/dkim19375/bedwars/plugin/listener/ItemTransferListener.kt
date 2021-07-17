/*
 * MIT License
 *
 * Copyright (c) 2021 dkim19375
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.util.isArmor
import me.dkim19375.bedwars.plugin.util.isTool
import me.dkim19375.bedwars.plugin.util.isWeapon
import me.dkim19375.itemmovedetectionlib.event.InventoryItemTransferEvent
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
        isCancelled = true
    }

    private fun InventoryItemTransferEvent.checkSpecialItems() {
        if (!type.isFromSelf) {
            return
        }
        if (items.map(ItemStack::getType).any(Material::isTool)) {
            isCancelled = true
            return
        }
        if (items.map(ItemStack::getType).any(Material::isWeapon)) {
            isCancelled = true
            return
        }
        if (items.map(ItemStack::getType).any(Material::isArmor)) {
            isCancelled = true
            return
        }
    }
}