package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.util.getPlayer
import me.dkim19375.bedwars.plugin.util.isArmor
import me.dkim19375.bedwars.plugin.util.isTool
import me.dkim19375.bedwars.plugin.util.isWeapon
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class InventoryClickListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler
    private fun InventoryClickEvent.onClick() {
        val game = plugin.gameManager.getGame(getPlayer())?: return
        game.upgradesManager.applyUpgrades(getPlayer())
        if (currentItem.type.isWeapon() || currentItem.type.isTool() || currentItem.type.isArmor()) {
            isCancelled = true
        }
        if (cursor.type.isWeapon() || cursor.type.isTool() || cursor.type.isArmor()) {
            isCancelled = true
        }
    }
}