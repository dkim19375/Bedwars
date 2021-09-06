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

package me.dkim19375.bedwars.plugin.api

import me.dkim19375.bedwars.api.BedwarsGameAPI
import me.dkim19375.bedwars.api.data.BedwarsGameData
import me.dkim19375.bedwars.api.data.BedwarsPlayerData
import me.dkim19375.bedwars.api.enumclass.GameState
import me.dkim19375.bedwars.api.enumclass.Result
import me.dkim19375.bedwars.api.enumclass.Team
import me.dkim19375.bedwars.plugin.manager.BedwarsGame
import me.dkim19375.bedwars.plugin.util.getPlayers
import me.dkim19375.dkimcore.extension.toImmutableMap
import me.dkim19375.dkimcore.extension.toImmutableSet
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

class BedwarsGameAPIImpl(private val game: BedwarsGame) : BedwarsGameAPI {
    override fun getState(): GameState = game.state

    override fun getCountdown(): Int = game.countdown

    override fun getElapsedTime(): Long = game.getElapsedTime() * 1000

    override fun getGamePlayers(): Map<Team, Set<Player>> = game.players.map {
        it.key to it.value.getPlayers()
    }.toImmutableMap()

    override fun getPlayers(): Set<Player> = game.getPlayersInGame().getPlayers().toImmutableSet()

    override fun getEliminated(): Set<Player> = game.eliminated.getPlayers().toImmutableSet()

    override fun getPlayersInLobby(): Set<Player> = game.playersInLobby.getPlayers().toImmutableSet()

    override fun getGameData(): BedwarsGameData = game.data

    override fun getBedStatus(): Map<Team, Boolean> = game.beds.toImmutableMap()

    override fun getBeforeData(): Map<Player, BedwarsPlayerData> = game.beforeData.map {
        Bukkit.getPlayer(it.key) to it.value as BedwarsPlayerData
    }.filter {
        it.first != null
    }.toImmutableMap()

    override fun getKills(): Map<Player, Int> = game.kills.map {
        Bukkit.getPlayer(it.key) to it.value
    }.filter {
        it.first != null
    }.toImmutableMap()

    override fun getTrackers(): Map<Player, Team> = game.trackers.map {
        Bukkit.getPlayer(it.key) to it.value
    }.filter {
        it.first != null
    }.toImmutableMap()

    override fun getSpectators(): Set<Player> = game.spectators.getPlayers()

    override fun start(): Result = start(false)

    override fun start(force: Boolean): Result = game.start(force)

    override fun stop(winner: Player?, team: Team) = game.stop(winner, team)

    override fun forceStop() = game.forceStop()

    override fun forceStop(whenDone: Runnable) = game.forceStop(whenDone::run)

    override fun isEditing(): Boolean = game.isEditing()

    override fun canStart(): Result = canStart(false)

    override fun canStart(force: Boolean): Result = game.canStart(force)

    override fun update() = update(false)

    override fun update(force: Boolean) = game.update(force)

    override fun addPlayer(player: Player): Result = game.addPlayer(player)

    override fun broadcast(text: String) = game.broadcast(text)

    override fun revertPlayer(player: Player) = game.revertPlayer(player)

    override fun leavePlayer(player: Player) = leavePlayer(player, true)

    override fun leavePlayer(player: Player, update: Boolean) = game.leavePlayer(player, update)

    override fun getTeamOfPlayer(player: Player): Team? = getTeamOfPlayer(player.uniqueId)

    override fun getTeamOfPlayer(player: UUID): Team? = game.getTeamOfPlayer(player)

    override fun saveMap() = game.saveMap()

    override fun regenerateMap() = game.regenerateMap()

    override fun regenerateMap(whenDone: Runnable) = game.regenerateMap(whenDone::run)

    override fun getPlayersInTeam(team: Team): Set<Player> = game.getPlayersInTeam(team).getPlayers().toImmutableSet()
}

fun BedwarsGame.getAPI(): BedwarsGameAPIImpl = BedwarsGameAPIImpl(this)