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