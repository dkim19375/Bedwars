package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent

class PlayerTeleportListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun PlayerTeleportEvent.onTeleport() {
        val world = to.world?: return
        val game = plugin.gameManager.getGame(player)?: return
        if (game.data.world.name != world.name) {
            player.sendMessage("${ChatColor.RED}You have left the bedwars game due to being teleported out!")
            game.leavePlayer(player)
        }
    }
}