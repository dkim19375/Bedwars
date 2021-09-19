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

import me.dkim19375.bedwars.api.enumclass.Team
import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.NEW_SOUND
import me.dkim19375.bedwars.plugin.config.MainConfigSettings
import me.dkim19375.bedwars.plugin.manager.BedwarsGame
import me.dkim19375.dkimbukkitcore.data.toWrapper
import me.dkim19375.dkimbukkitcore.function.getPlayers
import me.dkim19375.dkimbukkitcore.function.playSound
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import kotlin.math.abs
import kotlin.math.ceil

class ProjectileLaunchListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun ProjectileLaunchEvent.onLaunch() {
        checkPearl()
        checkBridgeEgg()
        checkBedBugs()
    }

    private fun ProjectileLaunchEvent.checkPearl() {
        if (entity.type != EntityType.ENDER_PEARL) {
            return
        }
        val shooter = entity.shooter as? Player ?: return
        val game = plugin.gameManager.getGame(shooter) ?: return
        game.getPlayersInGame().getPlayers().forEach {
            val sound = if (NEW_SOUND) Sound.valueOf("ENTITY_ENDERMEN_TELEPORT") else Sound.ENDERMAN_TELEPORT
            it.playSound(sound)
        }
    }

    private fun ProjectileLaunchEvent.getSpecialItemTeam(set: (BedwarsGame) -> MutableSet<UUID>): Pair<BedwarsGame, Team>? {
        val player = entity.shooter as? Player ?: return null
        val game = plugin.gameManager.getGame(player) ?: return null
        val team = game.getTeamOfPlayer(player) ?: return null
        if (player.uniqueId !in set(game)) {
            return null
        }
        set(game).remove(player.uniqueId)
        return game to team
    }

    private fun ProjectileLaunchEvent.checkBridgeEgg() {
        val player = entity.shooter as? Player ?: return
        val game = plugin.gameManager.getGame(player) ?: return
        val result = getSpecialItemTeam(BedwarsGame::bridgeEggs) ?: return
        val team = result.second

        @Suppress("DEPRECATION")
        val createBridge = create@{ entity: Entity ->
            val loc = entity.location.clone()
            val block = loc.block
            if (block.type != Material.AIR) {
                return@create
            }
            Bukkit.getScheduler().runTaskLater(plugin, {
                val playerLocs = player.world.players
                    .map(Player::getLocation)
                    .map(Location::getBlock)
                    .map(Block::getLocation)
                val newY = loc.y - 2.0
                val world = block.world
                val newX = ceil(loc.x)
                val newZ = ceil(loc.z)
                val block1 = Location(world, loc.x, newY, newZ).block
                val block2 = Location(world, newX, newY, loc.z).block
                val block3 = Location(world, newX, newY, newZ).block
                val blocks = setOf(block.location.subtract(0.0, 2.0, 0.0).block, block1, block2, block3).filterNot {
                    playerLocs.contains(it.location) || it.type != Material.AIR
                }
                for (loopBlock in blocks) {
                    loopBlock.type = Material.WOOL
                    loopBlock.data = team.color.data
                    game.placedBlocks[loopBlock.location.toWrapper()] =
                        plugin.shopConfigManager.getItemFromMaterial(Material.WOOL)
                }
                if (blocks.isNotEmpty()) {
                    player.playSound(if (NEW_SOUND) Sound.valueOf("ENTITY_CHICKEN_EGG") else Sound.CHICKEN_EGG_POP)
                }
            }, 3L)
        }
        entity.remove()
        @Suppress("UNCHECKED_CAST")
        val newEntity = player.launchProjectile(entityType.entityClass as Class<out Projectile>)
        object : BukkitRunnable() {
            private var amount = 0
            override fun run() {
                amount++
                if (newEntity.isDead || !newEntity.isValid || newEntity.location.y <= 2.0 || amount >= 40
                    || abs(newEntity.velocity.y) >= 1.0
                ) {
                    newEntity.remove()
                    cancel()
                    return
                }
                createBridge(newEntity)
            }
        }.runTaskTimer(plugin, 1L, 1L)
    }

    private fun ProjectileLaunchEvent.checkBedBugs() {
        val player = entity.shooter as? Player ?: return
        val result = getSpecialItemTeam(BedwarsGame::bedBugs) ?: return
        val game = result.first
        object : BukkitRunnable() {
            override fun run() {
                if (!entity.isDead && entity.isValid) {
                    return
                }
                cancel()
                val block = entity.location.block.getRelative(BlockFace.UP)
                game.createSpecialEntity(
                    block = block,
                    type = EntityType.SILVERFISH,
                    time = MainConfigSettings.TIME_BED_BUG,
                    showName = false,
                    player = player
                )
            }
        }.runTaskTimer(plugin, 1L, 1L)
    }
}