/*
 * MIT License
 *
 * Copyright (c) 2021 dkim19375
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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