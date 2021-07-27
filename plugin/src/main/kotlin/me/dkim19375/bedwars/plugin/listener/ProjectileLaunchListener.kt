package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.util.getPlayers
import me.dkim19375.bedwars.plugin.util.playSound
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.ProjectileLaunchEvent

class ProjectileLaunchListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun ProjectileLaunchEvent.onLaunch() {
        if (entity.type != EntityType.ENDER_PEARL) {
            return
        }
        val shooter = entity.shooter as? Player ?: return
        val game = plugin.gameManager.getGame(shooter) ?: return
        game.getPlayersInGame().getPlayers().forEach {
            it.playSound(Sound.ENDERMAN_TELEPORT)
        }
    }
}