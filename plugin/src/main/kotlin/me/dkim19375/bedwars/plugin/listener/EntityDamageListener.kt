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
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent

class EntityDamageListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private fun EntityDamageEvent.onDamage() {
        if (plugin.gameManager.getVillagersUUID().contains(entity.uniqueId)) {
            isCancelled = true
        }
        playerAutoRespawn()
        lobbyDamage()
        spectatorDamage()
    }

    private fun EntityDamageEvent.spectatorDamage() {
        val player = entity as? Player ?: return
        val game = plugin.gameManager.getGame(player) ?: return
        if (game.eliminated.contains(player.uniqueId)) {
            isCancelled = true
        }
    }

    private fun EntityDamageEvent.playerAutoRespawn() {
        val player = entity as? Player ?: return
        val game = plugin.gameManager.getGame(player) ?: return
        if (player.location.y > 1.0) {
            return
        }
        if (game.state == GameState.STARTED) {
            player.health = 0.01
            return
        }
    }

    private fun EntityDamageEvent.lobbyDamage() {
        val player = entity as? Player ?: return
        val game = plugin.gameManager.getGame(player) ?: return
        if (game.state != GameState.LOBBY && game.state != GameState.STARTING) {
            return
        }
        isCancelled = true
        return
    }
}