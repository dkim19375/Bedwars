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
import me.dkim19375.bedwars.plugin.util.toPotion
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class PotionConsumeListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private fun PlayerItemConsumeEvent.onEvent() {
        if (item.type != Material.POTION) {
            return
        }
        val potion = item.toPotion() ?: return
        val potionType = potion.type ?: return
        val type = potionType.effectType
        plugin.gameManager.getGame(player) ?: return
        Bukkit.getScheduler().runTask(plugin) {
            player.inventory.remove(Material.GLASS_BOTTLE)
        }
        when (type) {
            PotionEffectType.INVISIBILITY -> {
                player.addPotionEffect(PotionEffect(type, 2, 1), true)
                Bukkit.getScheduler().runTask(plugin) {
                    player.addPotionEffect(PotionEffect(type, 599, 0), true)
                }
                plugin.packetManager.hideArmor(player)
                return
            }
            PotionEffectType.SPEED -> {
                player.addPotionEffect(PotionEffect(type, 900, 1), true)
                return
            }
            PotionEffectType.JUMP -> {
                player.addPotionEffect(PotionEffect(type, 900, 4), true)
                return
            }
        }
    }
}