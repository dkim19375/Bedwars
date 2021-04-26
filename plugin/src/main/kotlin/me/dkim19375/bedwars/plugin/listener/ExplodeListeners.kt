package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.manager.BedwarsGame
import me.dkim19375.bedwars.plugin.util.getWrapper
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityExplodeEvent

class ExplodeListeners(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private fun EntityExplodeEvent.onExplode() {
        val uuid = plugin.gameManager.explosives[entity.uniqueId] ?: return
        plugin.gameManager.explosives.remove(entity.uniqueId)
        val game = plugin.gameManager.getGame(uuid)
        if (game == null) {
            isCancelled = true
            return
        }
        println("before: ${blockList()}")
        removeBlocks(blockList(), game)
        println("after: ${blockList()}")
    }

    private fun removeBlocks(blockList: MutableList<Block>, game: BedwarsGame) {
        for (block in blockList.toList()) {
            if (block.location.getWrapper() !in game.placedBlocks) {
                blockList.remove(block)
            }
            if (block.type == Material.BED) {
                blockList.remove(block)
            }
        }
    }
}