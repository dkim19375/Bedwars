package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler
    private fun PlayerQuitEvent.onQuit() {
        plugin.gameManager.invisPlayers.remove(player.uniqueId)
        plugin.gameManager.getGame(player)?: return
        player.damage(player.maxHealth * 2)
    }
}