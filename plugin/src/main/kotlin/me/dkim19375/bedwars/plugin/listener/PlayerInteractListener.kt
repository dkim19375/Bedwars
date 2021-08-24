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
import me.dkim19375.bedwars.plugin.gui.CompassGUI
import me.dkim19375.bedwars.plugin.gui.MainShopGUI
import me.dkim19375.bedwars.plugin.gui.TeleporterGUI
import me.dkim19375.bedwars.plugin.gui.UpgradeShopGUI
import me.dkim19375.bedwars.plugin.manager.BedwarsGame
import me.dkim19375.bedwars.plugin.util.default
import me.dkim19375.bedwars.plugin.util.isHologram
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

class PlayerInteractListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private fun PlayerInteractEvent.onInteract() {
        val game = plugin.gameManager.getGame(player) ?: return
        if (item?.type == Material.COMPASS) {
            CompassGUI(player, game).showPlayer()
            return
        }
        checkSpectator(game)
        gameOverItems(game)
        bedPrevention()
        setupFireballs()
    }

    private fun PlayerInteractEvent.checkSpectator(game: BedwarsGame) {
        if (!game.eliminated.contains(player.uniqueId)) {
            return
        }
        isCancelled = true
    }

    private fun PlayerInteractEvent.gameOverItems(game: BedwarsGame) {
        item ?: return
        if (!game.eliminated.contains(player.uniqueId)) {
            return
        }
        when (material) {
            Material.COMPASS -> TeleporterGUI(player, game).showPlayer()
            Material.PAPER -> {
                game.leavePlayer(player)
                Bukkit.dispatchCommand(player, "bedwars quickjoin")
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
            MainShopGUI(player, plugin).showPlayer()
            Bukkit.getScheduler().runTask(plugin) {
                MainShopGUI(player, plugin).showPlayer()
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
        if (!listOf(Material.BED, Material.BED_BLOCK).contains(clickedBlock.type)) {
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
        fireball.yield = 2.3f
        plugin.gameManager.addExplosive(fireball.uniqueId, player.uniqueId)
    }
}