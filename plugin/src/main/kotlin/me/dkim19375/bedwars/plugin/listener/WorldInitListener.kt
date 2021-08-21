package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldInitEvent

class WorldInitListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler(priority = EventPriority.HIGH)
    private fun WorldInitEvent.onInit() {
        plugin.gameManager.getGame(world) ?: return
        world.keepSpawnInMemory = false
    }
}