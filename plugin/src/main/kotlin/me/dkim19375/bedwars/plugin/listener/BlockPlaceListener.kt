package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.dkim19375core.data.LocationWrapper
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.TNTPrimed
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import kotlin.random.Random

private const val OFFSET = 0.10
class BlockPlaceListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private fun BlockPlaceEvent.onPlace() {
        val game = plugin.gameManager.getGame(player) ?: return
        if (block.type != Material.TNT) {
            game.placedBlocks.add(LocationWrapper(block.location))
            return
        }
        block.type = Material.AIR
        val tnt = block.world.spawn(block.location, TNTPrimed::class.java)
        val loc = block.location.clone()
        loc.x += Random.nextDouble(-OFFSET, OFFSET)
        loc.z += Random.nextDouble(-OFFSET, OFFSET)
        tnt.teleport(loc)
        Bukkit.getScheduler().runTask(plugin) {
            val newLoc = block.location.clone()
            newLoc.x += Random.nextDouble(-OFFSET, OFFSET)
            newLoc.z += Random.nextDouble(-OFFSET, OFFSET)
            tnt.teleport(loc)
        }
        tnt.fuseTicks = 60
        plugin.gameManager.addExplosive(tnt.uniqueId, player)
    }
}