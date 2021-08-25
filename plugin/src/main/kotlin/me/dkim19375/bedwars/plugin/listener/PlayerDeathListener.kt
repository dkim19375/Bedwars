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
import me.dkim19375.bedwars.plugin.enumclass.GameState
import me.dkim19375.bedwars.plugin.gui.MainShopGUI
import me.dkim19375.bedwars.plugin.gui.toCostType
import me.dkim19375.bedwars.plugin.util.dropItem
import me.dkim19375.bedwars.plugin.util.isWeapon
import me.dkim19375.dkimbukkitcore.function.logInfo
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class PlayerDeathListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler
    private fun PlayerDeathEvent.onDeath() {
        val game = plugin.gameManager.getGame(entity) ?: return
        if (game.state != GameState.STARTED) {
            return
        }
        val killer = entity.killer
        if (killer != null) {
            game.kills[killer.uniqueId] = game.kills.getOrElse(killer.uniqueId) { 0 } + 1
        }
        game.trackers.remove(entity.uniqueId)
        val location = entity.location.clone()
        entity.spigot().respawn() // auto respawn
        droppedExp = 0
        val originalDrops = drops.toList()
        val newDrops = drops.toMutableList()
        drops.clear()
        newDrops.removeIf { item ->
            !listOf(Material.IRON_INGOT, Material.GOLD_INGOT, Material.DIAMOND, Material.EMERALD).contains(item.type)
        }
        newDrops.removeIf { item ->
            item.type.isWeapon()
        }
        logInfo("drops: $newDrops")
        killer?.inventory?.let {
            it.addItem(*newDrops.toTypedArray())
            logInfo("added $newDrops to player inventory!")
        }
        val materials = mutableMapOf<MainShopGUI.CostType, Int>()
        for (drop in newDrops) {
            val cost = drop.type.toCostType() ?: continue
            materials[cost] = materials.getOrDefault(cost, 0) + drop.amount
        }
        killer ?: logInfo("killer is null!")
        killer ?: newDrops.forEach { drop -> location.dropItem(drop) }
        game.playerKilled(entity, originalDrops)
        for ((cost, amount) in materials) {
            killer?.sendMessage("${cost.color}+$amount ${cost.displayname}")
        }
    }
}