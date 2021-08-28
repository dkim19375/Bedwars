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
import me.dkim19375.bedwars.plugin.util.isHologram
import org.bukkit.ChatColor
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Arrow
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.util.Vector
import kotlin.math.roundToInt

class EntityDamageByEntityListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private fun EntityDamageByEntityEvent.onDamage() {
        checkExplosive()
        checkHolo()
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun EntityDamageByEntityEvent.checkArrow() {
        plugin.gameManager.getGame(entity.uniqueId) ?: return
        val player = entity as? Player ?: return
        val damager = damager as? Arrow ?: return
        val shooter = damager.shooter as? Player ?: return
        if (player.uniqueId == shooter.uniqueId) {
            return
        }
        if (player.health - finalDamage <= 0.0) {
            return
        }
        val newHp = (((player.health - finalDamage) * 10.0).roundToInt() / 10.0).let {
            if (it.toInt().toDouble() == it) {
                it.toInt().toString()
            } else {
                it.toString()
            }
        }
        shooter.sendMessage("${player.displayName} ${ChatColor.GRAY}is on ${ChatColor.RED}$newHp ${ChatColor.GRAY}HP!")
    }

    private fun EntityDamageByEntityEvent.checkExplosive() {
        plugin.gameManager.getGame(entity.uniqueId) ?: return
        if (entity !is Player) {
            return
        }
        val damager = damager
        if (damager.type != EntityType.PRIMED_TNT && damager.type != EntityType.FIREBALL) {
            return
        }
        val entities = plugin.gameManager.getExplosives().keys
        if (damager.uniqueId !in entities) {
            return
        }
        damage = finalDamage * 0.1
        val newVelocity = entity.velocity.clone().multiply(1.7)
        entity.velocity = Vector(newVelocity.x.coerce(), newVelocity.y.coerce(), newVelocity.z.coerce())
    }

    private fun Double.coerce(): Double = coerceIn(-3.9999999999999999, 3.9999999999999999)

    private fun EntityDamageByEntityEvent.checkHolo() {
        val clicked = entity as? ArmorStand ?: return
        if (clicked.isHologram()) {
            isCancelled = true
        }
    }
}