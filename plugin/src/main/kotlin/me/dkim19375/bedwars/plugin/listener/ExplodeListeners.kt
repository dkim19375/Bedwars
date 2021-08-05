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
import me.dkim19375.bedwars.plugin.manager.BedwarsGame
import me.dkim19375.bedwars.plugin.util.getWrapper
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityExplodeEvent

class ExplodeListeners(private val plugin: BedwarsPlugin) : Listener {

    @EventHandler(priority = EventPriority.HIGH)
    private fun EntityExplodeEvent.onExplode() {
        val explosives = plugin.gameManager.getExplosives()
        val uuid = if (explosives[entity.uniqueId] == null) {
            ((entity as? Projectile)?.shooter as? Entity)?.uniqueId
        } else {
            explosives[entity.uniqueId]
        } ?: return
        plugin.gameManager.removeExplosive(entity.uniqueId)
        val game = plugin.gameManager.getGame(uuid)
        if (game == null) {
            isCancelled = true
            return
        }
        removeBlocks(blockList(), game)
    }

    private fun removeBlocks(blockList: MutableList<Block>, game: BedwarsGame) {
        for (block in blockList.toList()) {
            if (block.location.getWrapper() !in game.placedBlocks) {
                blockList.remove(block)
            }
            if (setOf(Material.BED_BLOCK, Material.BED, Material.ENDER_STONE).contains(block.type)) {
                blockList.remove(block)
            }
        }
    }
}