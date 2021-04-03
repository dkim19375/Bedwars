package me.dkim19375.bedwars.listener

import me.dkim19375.bedwars.BedwarsPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler
    private fun PlayerQuitEvent.onQuit() {
        plugin.gameManager.getRunningGames().forEach { (_, game) ->
            game.leavePlayer(player)
        }
        plugin.gameManager.invisPlayers.remove(player.uniqueId)
    }
}