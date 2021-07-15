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
import me.dkim19375.bedwars.plugin.enumclass.GameState
import me.dkim19375.bedwars.plugin.manager.BedwarsGame
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import java.util.*

class PartiesListeners(private val plugin: BedwarsPlugin) : Listener {
    private val api = plugin.partiesAPI
    private val teleports = mutableSetOf<UUID>()

    fun onGameJoin(player: Player, game: BedwarsGame): Boolean {
        val api = api ?: return true
        if (!teleports.contains(player.uniqueId)) {
            return true
        }
        val partyPlayer = api.getPartyPlayer(player.uniqueId) ?: return true
        val party = api.getParty(partyPlayer.partyId ?: return true) ?: return true
        val partyPlayers = party.onlineMembers.filter { p -> !game.getPlayersInGame().contains(p.playerUUID) }
        val players = partyPlayers.mapNotNull { p -> Bukkit.getPlayer(p.playerUUID) }
        if ((players.size + 1) > (game.data.maxPlayers - game.playersInLobby.size)) {
            player.sendMessage("The party is too large to join the game!")
            return false
        }
        players.forEach(game::addPlayer)
        return true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private fun PlayerCommandPreprocessEvent.onCommand() {
        val api = api ?: return
        val command = message.replace("/", "").trim()
        val game = plugin.gameManager.getGame(player) ?: return
        if (!command.equals("party teleport", ignoreCase = true)) {
            return
        }
        if (!player.hasPermission("parties.user.teleport")) {
            return
        }
        val partyPlayer = api.getPartyPlayer(player.uniqueId) ?: return
        if (!partyPlayer.isInParty) {
            return
        }
        val partyId = partyPlayer.partyId ?: return
        val party = api.getParty(partyId) ?: return
        if (game.state != GameState.LOBBY && game.state != GameState.STARTING) {
            player.sendMessage("You must be in the lobby!")
            isCancelled = true
            return
        }
        if ((party.onlineMembers.size - 1) > (game.data.maxPlayers - game.playersInLobby.size)) {
            isCancelled = true
            player.sendMessage("The party is too large to join the game!")
            return
        }
        teleports.add(player.uniqueId)
    }
}