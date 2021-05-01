package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.event.PlayerCoordsChangeEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PlayerCoordsChangeListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler(ignoreCancelled = true)
    private fun PlayerCoordsChangeEvent.onMove() {
        val game = plugin.gameManager.getGame(player)?: return
        game.upgradesManager.triggerTrap(player)
    }
}