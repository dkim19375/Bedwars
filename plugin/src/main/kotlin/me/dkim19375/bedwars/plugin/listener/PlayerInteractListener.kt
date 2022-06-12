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

import me.dkim19375.bedwars.api.enumclass.GameState
import me.dkim19375.bedwars.api.enumclass.SpecialItemType
import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.config.MainConfigSettings
import me.dkim19375.bedwars.plugin.gui.CompassGUI
import me.dkim19375.bedwars.plugin.gui.MainShopGUI
import me.dkim19375.bedwars.plugin.gui.TeleporterGUI
import me.dkim19375.bedwars.plugin.gui.UpgradeShopGUI
import me.dkim19375.bedwars.plugin.manager.BedwarsGame
import me.dkim19375.bedwars.plugin.util.*
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Fireball
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.material.SpawnEgg
import java.util.*

class PlayerInteractListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    private fun PlayerInteractEvent.onInteract() {
        val game = plugin.gameManager.getGame(player) ?: return
        checkTracker(game)
        checkSpectator(game)
        gameOverItems(game)
        bedPrevention()
        setupFireballs()
        checkSpecialType(SpecialItemType.BED_BUGS, game.bedBugs)
        checkSpecialType(SpecialItemType.BRIDGE_EGGS, game.bridgeEggs)
        checkDreamDefenders(game)
    }

    private fun PlayerInteractEvent.checkSpecialType(
        type: SpecialItemType,
        map: MutableMap<UUID, Int>,
    ) {
        if (material == Material.AIR) {
            return
        }
        if (action !in setOf(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK)) {
            return
        }
        val configItem = item?.getConfigItem() ?: return
        if (configItem.specialItem != type) {
            return
        }
        item.type.getProjectileType() ?: return
        val value = map[player.uniqueId]
        map[player.uniqueId] = (value ?: 0) + 1
    }

    private fun PlayerInteractEvent.checkDreamDefenders(game: BedwarsGame) {
        if (material == Material.AIR) {
            return
        }
        if (action !in setOf(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK)) {
            return
        }
        val configItem = item?.getConfigItem() ?: return
        if (configItem.specialItem != SpecialItemType.DREAM_DEFENDER) {
            return
        }
        if (action.name.startsWith("LEFT")) {
            return
        }
        if (item.type != Material.MONSTER_EGG) {
            return
        }
        val block = blockFace?.let { clickedBlock?.getRelative(it) } ?: return
        val type = (item.data as? SpawnEgg)?.spawnedType ?: return
        isCancelled = true
        if (item.amount <= 1) {
            item.type = Material.AIR
        } else {
            item.amount = item.amount - 1
        }
        game.createSpecialEntity(block, type, MainConfigSettings.TIME_DREAM_DEFENDER, true, player)
    }

    private fun PlayerInteractEvent.checkTracker(game: BedwarsGame) {
        if (game.state != GameState.STARTED) {
            return
        }
        if (item?.type != Material.COMPASS) {
            return
        }
        CompassGUI(player, game).showPlayer()
        isCancelled = true
    }

    private fun PlayerInteractEvent.checkSpectator(game: BedwarsGame) {
        if (!game.eliminated.contains(player.uniqueId)) {
            return
        }
        isCancelled = true
    }

    private fun PlayerInteractEvent.gameOverItems(game: BedwarsGame) {
        item ?: return
        if (!game.eliminated.contains(player.uniqueId) && game.state != GameState.GAME_END) {
            return
        }
        when (material) {
            Material.COMPASS -> TeleporterGUI(player, game).showPlayer()
            Material.PAPER -> {
                game.leavePlayer(player)
                player.chat("/bedwars quickjoin")
            }
            Material.BED -> game.leavePlayer(player)
            else -> return
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun PlayerInteractAtEntityEvent.onInteractAtEntity() {
        val game = plugin.gameManager.getGame(player) ?: return
        checkShops(game)
        checkHolograms()
    }

    private fun PlayerInteractAtEntityEvent.checkShops(game: BedwarsGame) {
        if (game.npcManager.getShopVillagersUUID().contains(rightClicked.uniqueId)) {
            isCancelled = true
            MainShopGUI(player, plugin, game).showPlayer()
            Bukkit.getScheduler().runTask(plugin) {
                MainShopGUI(player, plugin, game).showPlayer()
            }
            return
        }
        if (!game.npcManager.getUpgradeVillagersUUID().contains(rightClicked.uniqueId)) {
            return
        }
        val team = game.getTeamOfPlayer(player) ?: return
        isCancelled = true
        UpgradeShopGUI(player, team, plugin).showPlayer()
        Bukkit.getScheduler().runTask(plugin) {
            UpgradeShopGUI(player, team, plugin).showPlayer()
        }
    }

    private fun PlayerInteractAtEntityEvent.checkHolograms() {
        val rightClicked = rightClicked as? ArmorStand ?: return
        if (rightClicked.isHologram()) {
            isCancelled = true
        }
    }

    private fun PlayerInteractEvent.bedPrevention() {
        plugin.gameManager.getGame(player) ?: return
        clickedBlock ?: return
        if (!clickedBlock.type.isBed()) {
            return
        }
        if (!action.name.startsWith("RIGHT")) {
            return
        }
        if (player.isSneaking) {
            if (player.itemInHand.type.default(Material.AIR) != Material.AIR) {
                return
            }
        }
        isCancelled = true
    }

    private fun PlayerInteractEvent.setupFireballs() {
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return
        }
        if (player.itemInHand.type != Material.FIREBALL) {
            return
        }
        if (player.isSneaking && action == Action.RIGHT_CLICK_BLOCK) {
            return
        }
        isCancelled = true
        if (player.itemInHand.amount == 1) {
            player.itemInHand = ItemStack(Material.AIR)
        } else {
            player.itemInHand = ItemStack(player.itemInHand.type, player.itemInHand.amount - 1)
        }
        val fireball = player.launchProjectile(Fireball::class.java)
        fireball.setIsIncendiary(true)
        fireball.teleport(fireball.location.clone().subtract(0.0, 0.5, 0.0))
        fireball.yield = 2.3f
        plugin.gameManager.addExplosive(fireball.uniqueId, player.uniqueId)
    }
}