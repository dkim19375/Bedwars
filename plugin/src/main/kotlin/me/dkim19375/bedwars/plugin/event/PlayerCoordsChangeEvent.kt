package me.dkim19375.bedwars.plugin.event

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

class PlayerCoordsChangeEvent(player: Player, val from: Location, val to: Location, private var cancelState: Boolean) : PlayerEvent(player), Cancellable {
    companion object {
        private val HANDLERS = HandlerList()
        @JvmStatic
        fun getHandlerList() = HANDLERS
    }

    override fun getHandlers() = HANDLERS
    override fun isCancelled() = cancelState

    override fun setCancelled(cancel: Boolean) {
        cancelState = cancel
    }
}