package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.util.getPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class PlayerInteractListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler
    private fun InventoryClickEvent.onClick() {
        val game = plugin.gameManager.getGame(getPlayer())?: return
        game.upgradesManager.applyUpgrades(getPlayer())
    }
}