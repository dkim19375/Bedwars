package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.gui.MainShopGUI
import me.dkim19375.bedwars.plugin.gui.UpgradeShopGUI
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType

class PlayerInventoryListener(private val plugin: BedwarsPlugin) : Listener {
    // @EventHandler
    private fun InventoryOpenEvent.onOpen() {
        val player = player as Player
        val game = plugin.gameManager.getGame(player)?: return
        val villager = inventory.holder
        if (villager !is Villager) {
            return
        }
        if (inventory.type != InventoryType.CHEST) {
            return
        }
        if (game.data.shopVillagers.contains(villager.uniqueId)) {
            isCancelled = true
            Bukkit.getScheduler().runTask(plugin) {
                MainShopGUI(player, plugin).showPlayer()
            }
            return
        }
        if (!game.data.upgradeVillagers.contains(villager.uniqueId)) {
            return
        }
        val team = game.getTeamOfPlayer(player)?: return
        isCancelled = true
        Bukkit.getScheduler().runTask(plugin) {
            UpgradeShopGUI(player, team, plugin).showPlayer()
        }
    }
}