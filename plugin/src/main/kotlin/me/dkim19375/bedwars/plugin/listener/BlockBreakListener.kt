/*
 *     Bedwars, a minigame for spigot
 *     Copyright (C) 2021  dkim19375
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.enumclass.GameState
import me.dkim19375.bedwars.plugin.util.getBedHead
import me.dkim19375.bedwars.plugin.util.isBed
import me.dkim19375.bedwars.plugin.util.setConfigItem
import me.dkim19375.dkimbukkitcore.data.LocationWrapper
import me.dkim19375.dkimbukkitcore.data.toWrapper
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
        if (!block.type.isBed()) {
            val loc = LocationWrapper(block.location)
            if (loc in game.placedBlocks) {
                val configItem = game.placedBlocks[loc]
                isCancelled = true
                block.drops.forEach { block.world.dropItem(block.location, it.setConfigItem(configItem)) }
                block.type = Material.AIR
                return
            }
            isCancelled = true
            return
        }
        val team = game.getTeamOfPlayer(player) ?: return
        val location = block.getBedHead()
        val beds = game.data.beds
        val bed = beds.firstOrNull { data -> data.location.toWrapper() == location.toWrapper() } ?: return
        if (bed.team == team) {
            player.sendMessage("${ChatColor.RED}You cannot break your own bed!")
            isCancelled = true
            return
        }
        isCancelled = true
        Bukkit.getScheduler().runTask(plugin) {
            player.getNearbyEntities(6.0, 6.0, 6.0)
                .mapNotNull { i -> i as? Item }
                .filter { i -> i.itemStack.type.isBed() }
                .forEach(Item::remove)
        }
        block.type = Material.AIR
        block.state.update()
        game.bedBreak(bed.team, player)
    }
}