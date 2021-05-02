package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.gui.MainShopGUI
import me.dkim19375.bedwars.plugin.gui.UpgradeShopGUI
import me.dkim19375.bedwars.plugin.util.default
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
        blockPrevention()
        setupFireballs()
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun PlayerInteractAtEntityEvent.onInteractAtEntity() {
        val game = plugin.gameManager.getGame(player) ?: return
        if (game.npcManager.getShopVillagersUUID().contains(rightClicked.uniqueId)) {
            isCancelled = true
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
        Bukkit.getScheduler().runTask(plugin) {
            UpgradeShopGUI(player, team, plugin).showPlayer()
        }
    }

    private fun PlayerInteractEvent.blockPrevention() {
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
        fireball.yield = 2.5f
        plugin.gameManager.addExplosive(fireball.uniqueId, player.uniqueId)
    }
}