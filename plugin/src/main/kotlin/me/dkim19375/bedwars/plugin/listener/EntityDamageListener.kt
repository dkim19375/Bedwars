package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.enumclass.GameState
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent

class EntityDamageListener(private val plugin: BedwarsPlugin) : Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private fun EntityDamageEvent.onDamage() {
        if (plugin.gameManager.getVillagersUUID().contains(entity.uniqueId)) {
            isCancelled = true
        }
        playerAutoRespawn()
    }

    private fun EntityDamageEvent.playerAutoRespawn() {
        val player = entity as? Player ?: return
        val game = plugin.gameManager.getGame(player) ?: return
        if (game.state == GameState.STARTED) {
            player.health = 0.0
            return
        }
        if (game.state == GameState.LOBBY || game.state == GameState.STARTING) {
            isCancelled = true
            return
        }
    }
}