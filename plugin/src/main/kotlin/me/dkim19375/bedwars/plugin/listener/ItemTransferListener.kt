package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.util.isArmor
import me.dkim19375.bedwars.plugin.util.isTool
import me.dkim19375.bedwars.plugin.util.isWeapon
import me.dkim19375.itemmovedetectionlib.event.InventoryItemTransferEvent
import me.dkim19375.itemmovedetectionlib.util.TransferType
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

class ItemTransferListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler(ignoreCancelled = true)
    private fun InventoryItemTransferEvent.onTransfer() {
        plugin.gameManager.getGame(player)?: return
        checkArmor()
        checkSpecialItems()
    }

    private fun InventoryItemTransferEvent.checkArmor() {
        if (items.map(ItemStack::getType).none(Material::isArmor)) {
            return
        }
        isCancelled = true
    }

    private fun InventoryItemTransferEvent.checkSpecialItems() {
        if (!(type == TransferType.PUT_SELF || !type.isPut)) {
            return
        }
        if (items.map(ItemStack::getType).any(Material::isTool)) {
            isCancelled = true
        }
        if (items.map(ItemStack::getType).any(Material::isWeapon)) {
            isCancelled = true
        }
    }
}