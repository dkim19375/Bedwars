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

import com.alessiodp.parties.api.events.bukkit.player.BukkitPartiesPlayerPreTeleportEvent
import com.alessiodp.parties.api.interfaces.PartyPlayer
import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.enumclass.GameState
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class PartiesListeners(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private fun BukkitPartiesPlayerPreTeleportEvent.onTeleport() {
        val player = Bukkit.getPlayer(partyPlayer.playerUUID) ?: return
        val game = plugin.gameManager.getGame(partyPlayer.playerUUID) ?: return
        val players = party.getOnlineMembers(true)
        if (game.state != GameState.LOBBY && game.state != GameState.STARTING) {
            player.sendMessage("You must be in the lobby!")
            isCancelled = true
            return
        }
        if ((players.size - 1) > (game.data.maxPlayers - game.playersInLobby.size)) {
            isCancelled = true
            player.sendMessage("The party is too large to join the game!")
            return
        }
        for (partyPlayer in players.map(PartyPlayer::getPlayerUUID).map(Bukkit::getPlayer)) {
            plugin.gameManager.getGame(partyPlayer)?.leavePlayer(partyPlayer)
            game.addPlayer(partyPlayer)
        }
    }
}