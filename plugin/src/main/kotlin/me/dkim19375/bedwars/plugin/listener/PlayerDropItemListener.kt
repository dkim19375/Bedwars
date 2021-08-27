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
import me.dkim19375.bedwars.plugin.util.isArmor
import me.dkim19375.bedwars.plugin.util.isTool
import me.dkim19375.bedwars.plugin.util.isWeapon
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent

class PlayerDropItemListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private fun PlayerDropItemEvent.onDrop() {
        val game = plugin.gameManager.getGame(player) ?: return
        val type = itemDrop.itemStack.type
        if (type.isArmor() || type.isTool() || type.isWeapon()) {
            isCancelled = true
        }
        if (type == Material.COMPASS) {
            isCancelled = true
        }
        if (game.eliminated.contains(player.uniqueId) || game.state == GameState.GAME_END) {
            isCancelled = true
        }
    }
}