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
import me.dkim19375.bedwars.plugin.config.MainConfigSettings
import me.dkim19375.bedwars.plugin.manager.BedwarsGame
import me.dkim19375.dkimbukkitcore.data.toWrapper
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Chicken
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.math.ceil

class EntityExplodeListener(private val plugin: BedwarsPlugin) : Listener {

    @EventHandler(priority = EventPriority.HIGH)
    private fun EntityExplodeEvent.onExplode() {
        val explosives = plugin.gameManager.getExplosives()
        val uuid = if (explosives[entity.uniqueId] == null) {
            ((entity as? Projectile)?.shooter as? Player)?.uniqueId
        } else {
            explosives[entity.uniqueId]
        } ?: return
        plugin.gameManager.removeExplosive(entity.uniqueId)
        val game = plugin.gameManager.getGame(uuid)
        if (game == null) {
            isCancelled = true
            return
        }
        removeBlocks(entity.location, blockList(), game)
    }

    private fun removeBlocks(explosion: Location, blockList: MutableList<Block>, game: BedwarsGame) {
        val entity = explosion.world.spawn(explosion, Chicken::class.java).apply {
            canPickupItems = false
            setOf(PotionEffectType.INVISIBILITY, PotionEffectType.SLOW).forEach {
                addPotionEffect(PotionEffect(it, 2, 255, false, false))
            }
        }
        val materials = Material.values().toSet()
            .minus(listOf(Material.GLASS, Material.STAINED_GLASS, Material.BED, Material.BED_BLOCK).toSet())
        for (block in blockList.toList()) {
            if (block.location?.toWrapper() !in game.placedBlocks && plugin.mainConfigManager.get(MainConfigSettings.MAP_PROTECTION)) {
                blockList.remove(block)
                continue
            }
            if (block.type in setOf(Material.BED_BLOCK, Material.BED, Material.GLASS, Material.STAINED_GLASS)) {
                blockList.remove(block)
                continue
            }
            val distance = ceil(explosion.distance(block.location)).toInt()
            entity.teleport(entity.location.clone()
                .setDirection(block.location.subtract(entity.location.toVector()).toVector()))
            val blocks = entity.getLineOfSight(materials, distance)
            if (blocks.any { it.location == block.location }) {
                continue
            }
            for (blockLine in blocks) {
                if (!shouldBeRemoved(block, game)) {
                    break
                }
                blockList.remove(block)
            }
        }
        entity.remove()
    }

    private fun shouldBeRemoved(block: Block, game: BedwarsGame): Boolean {
        if (block.location?.toWrapper() !in game.placedBlocks && plugin.mainConfigManager.get(MainConfigSettings.MAP_PROTECTION)) {
            return false
        }
        if (block.type in setOf(Material.BED_BLOCK, Material.BED, Material.GLASS, Material.STAINED_GLASS)) {
            return false
        }
        return true
    }
}