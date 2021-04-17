package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent

class EntityDamageListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private fun EntityDamageEvent.onDamage() {
        val games = plugin.gameManager.getGames().values.toList()
        for (game in games) {
            val gameData = game.data
            if (gameData.shopVillagers.plus(gameData.upgradeVillagers).contains(entity.uniqueId)) {
                isCancelled = true
            }
        }
    }
}