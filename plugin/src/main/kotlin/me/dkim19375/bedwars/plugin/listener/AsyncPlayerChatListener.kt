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

import me.dkim19375.bedwars.api.enumclass.GameState
import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.util.setGray
import me.dkim19375.dkimbukkitcore.function.getPlayers
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class AsyncPlayerChatListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private fun AsyncPlayerChatEvent.onChat() {
        val game = plugin.gameManager.getGame(player) ?: return
        if (player.gameMode != GameMode.SPECTATOR
            && game.state != GameState.GAME_END
            && !game.eliminated.contains(player.uniqueId)
        ) {
            return
        }
        val new = recipients.map(Player::getUniqueId)
            .toSet()
            .getPlayers()
            .filter {
                it.gameMode == GameMode.SPECTATOR
                        || game.state == GameState.GAME_END
                        || game.eliminated.contains(it.uniqueId)
            }
        try {
            recipients.clear()
        } catch (_: UnsupportedOperationException) {
            isCancelled = true
            return
        }
        format = "[SPECTATOR] ${ChatColor.RESET}$format".setGray()
        recipients.addAll(new)
    }
}