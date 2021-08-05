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
        val killer = entity.killer
        killer?.inventory?.let {
            it.addItem(*newDrops.toTypedArray())
            logInfo("added $newDrops to player inventory!")
        }
        killer ?: logInfo("killer is null!")
        killer ?: newDrops.forEach { drop -> location.dropItem(drop) }
        game.playerKilled(entity, originalDrops)
    }
}