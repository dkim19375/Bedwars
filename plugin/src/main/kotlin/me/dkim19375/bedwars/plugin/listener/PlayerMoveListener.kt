package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.event.PlayerCoordsChangeEvent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class PlayerMoveListener : Listener {
    @EventHandler
    private fun PlayerMoveEvent.onMove() {
        try {
            if (from.distance(to) > 0) {
                val event = PlayerCoordsChangeEvent(player, from, to, isCancelled)
                Bukkit.getPluginManager().callEvent(event)
                isCancelled = event.isCancelled
            }
        } catch (_: IllegalArgumentException) {
        }
    }
}