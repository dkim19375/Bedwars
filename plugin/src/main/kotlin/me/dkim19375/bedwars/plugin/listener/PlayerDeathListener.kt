package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class PlayerDeathListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler
    private fun PlayerDeathEvent.onDeath() {
        val game = plugin.gameManager.getGame(entity)?: return
        game.playerKilled(entity)
    }
}