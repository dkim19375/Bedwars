package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.enumclass.GameState
import me.dkim19375.bedwars.plugin.util.getBedHead
import me.dkim19375.bedwars.plugin.util.getWrapper
import me.dkim19375.dkim19375core.data.LocationWrapper
import me.dkim19375.dkim19375core.function.filterNonNull
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class BlockBreakListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private fun BlockBreakEvent.onBreak() {
        val game = plugin.gameManager.getGame(player) ?: return
        if (game.state == GameState.LOBBY || game.state == GameState.STARTING) {
            isCancelled = true
            return
        }
        if (block.type != Material.BED_BLOCK && block.type != Material.BED) {
            if (LocationWrapper(block.location) in game.placedBlocks) {
                return
            }
            isCancelled = true
            return
        }
        val team = game.getTeamOfPlayer(player)?: return
        val location = block.getBedHead()
        val beds = game.data.beds
        val bed = beds.firstOrNull { data -> data.location.getWrapper() == location.getWrapper() }?: return
        if (bed.team == team) {
            player.sendMessage("${ChatColor.RED}You cannot break your own bed!")
            isCancelled = true
            return
        }
        isCancelled = true
        Bukkit.getScheduler().runTask(plugin) {
            player.getNearbyEntities(6.0, 6.0, 6.0)
                .map { i -> i as? Item }
                .filterNonNull()
                .filter { i -> i.itemStack.type == Material.BED }
                .forEach(Item::remove)
        }
        block.type = Material.AIR
        block.state.update()
        game.bedBreak(bed.team, player)
    }
}