package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.enumclass.GameState
import me.dkim19375.bedwars.plugin.util.dropItem
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class PlayerDeathListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler
    private fun PlayerDeathEvent.onDeath() {
        val game = plugin.gameManager.getGame(entity) ?: return
        if (game.state != GameState.STARTED) {
            return
        }
        entity.spigot().respawn() // auto respawn
        droppedExp = 0
        val originalDrops = drops.toList()
        val newDrops = drops.toMutableList()
        drops.clear()
        newDrops.removeIf { item ->
            !listOf(Material.IRON_INGOT, Material.GOLD_INGOT, Material.DIAMOND, Material.EMERALD).contains(item.type)
        }
        val location = entity.location.clone()
        newDrops.forEach { drop -> location.dropItem(drop) }
        game.playerKilled(entity, originalDrops)
    }
}