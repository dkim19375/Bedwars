package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import org.bukkit.Material
import org.bukkit.entity.Fireball
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class PlayerInteractListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private fun PlayerInteractEvent.onInteract() {
        setupFireballs()
    }

    private fun PlayerInteractEvent.setupFireballs() {
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return
        if (player.itemInHand.type != Material.FIREBALL) {
            return
        }
        isCancelled = true
        player.itemInHand = ItemStack(Material.AIR)
        val defaultBall = player.launchProjectile(Fireball::class.java)
        defaultBall.yield = 0F
        val defaultVelocity = defaultBall.velocity
        defaultBall.remove()
        val velocity = defaultVelocity.multiply(1.6)
        val newLoc = player.getLineOfSight(setOf(), 2)[1].location
            .setDirection(player.location.direction)

        val fireball = player.world.spawn(newLoc, defaultBall.javaClass)
        fireball.setIsIncendiary(true)
        fireball.velocity = velocity
        plugin.gameManager.explosives[fireball.uniqueId] = player.uniqueId
    }
}