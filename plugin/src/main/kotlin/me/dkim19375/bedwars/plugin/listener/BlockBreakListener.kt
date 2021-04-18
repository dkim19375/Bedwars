package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.data.LocationWrapper
import me.dkim19375.bedwars.plugin.util.getBedHead
import me.dkim19375.bedwars.plugin.util.getWrapper
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class BlockBreakListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private fun BlockBreakEvent.onBreak() {
        val game = plugin.gameManager.getGame(player) ?: return
        if (block.type == Material.BED_BLOCK) {
            val team = game.getTeamOfPlayer(player)?: return
            val location = block.getBedHead()
            val beds = game.data.beds
            val bed = beds.firstOrNull { data -> data.location.getWrapper() == location.getWrapper() }?: return
            if (bed.team == team) {
                player.sendMessage("${ChatColor.RED}You cannot break your own bed!")
                isCancelled = true
                return
            }
            game.bedBreak(bed.team, player)
            return
        }
        if (LocationWrapper(block.location) in game.placedBlocks) {
            return
        }
        isCancelled = true
    }
}