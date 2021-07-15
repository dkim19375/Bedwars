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