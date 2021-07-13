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
import me.dkim19375.bedwars.plugin.gui.MainShopGUI
import me.dkim19375.bedwars.plugin.gui.UpgradeShopGUI
import me.dkim19375.bedwars.plugin.util.default
import me.dkim19375.bedwars.plugin.util.isArmor
import me.dkim19375.bedwars.plugin.util.isTool
import me.dkim19375.bedwars.plugin.util.isWeapon
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Fireball
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class PlayerInteractListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    private fun PlayerInteractEvent.onInteract() {
        plugin.gameManager.getGame(player) ?: return
        makeItemUnbreakable()
        bedPrevention()
        setupFireballs()
    }

    private fun PlayerInteractEvent.makeItemUnbreakable() {
        item ?: return
        if (item.type.isTool() || item.type.isArmor() || item.type.isWeapon()) {
            item.durability = 0
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun PlayerInteractAtEntityEvent.onInteractAtEntity() {
        val game = plugin.gameManager.getGame(player) ?: return
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