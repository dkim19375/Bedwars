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
import me.dkim19375.bedwars.plugin.util.teleportUpdated
import me.dkim19375.dkimbukkitcore.data.LocationWrapper
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.TNTPrimed
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import kotlin.random.Random

private const val OFFSET = 0.01

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
        tnt.teleportUpdated(loc)
        Bukkit.getScheduler().runTask(plugin) {
            val newLoc = block.location.clone()
            newLoc.x += Random.nextDouble(-OFFSET, OFFSET)
            newLoc.z += Random.nextDouble(-OFFSET, OFFSET)
            tnt.teleportUpdated(loc)
        }
        tnt.fuseTicks = 50
        plugin.gameManager.addExplosive(tnt.uniqueId, player)
    }
}