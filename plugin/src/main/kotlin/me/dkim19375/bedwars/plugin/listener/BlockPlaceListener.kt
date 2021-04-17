package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.data.LocationWrapper
import org.bukkit.Material
import org.bukkit.entity.TNTPrimed
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent

class BlockPlaceListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private fun BlockPlaceEvent.onPlace() {
        val game = plugin.gameManager.getGame(player)?: return
        if (block.type == Material.TNT) {
            isCancelled = true
            val tnt = block.world.spawn(block.location, TNTPrimed::class.java)
            plugin.gameManager.explosives[tnt.uniqueId] = player.uniqueId
            return
        }
        game.placedBlocks.add(LocationWrapper(block.location))
    }
}