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

@file:Suppress("UNUSED_PARAMETER")

package me.dkim19375.bedwars.plugin.manager

import me.dkim19375.bedwars.api.enumclass.GameState
import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.data.SpawnerData
import me.dkim19375.bedwars.plugin.enumclass.SpawnerType
import me.dkim19375.bedwars.plugin.util.Delay
import me.dkim19375.bedwars.plugin.util.formatTime
import me.dkim19375.bedwars.plugin.util.toRomanNumeral
import me.tigerhix.lib.scoreboard.ScoreboardLib
import me.tigerhix.lib.scoreboard.common.EntryBuilder
import me.tigerhix.lib.scoreboard.type.Entry
import me.tigerhix.lib.scoreboard.type.ScoreboardHandler
import me.tigerhix.lib.scoreboard.type.SimpleScoreboard
import org.apache.commons.lang.StringUtils
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import java.util.*

class ScoreboardManager(private val plugin: BedwarsPlugin) : ScoreboardHandler, Listener {
    private val scoreboards = mutableMapOf<UUID, SimpleScoreboard>()

    fun getScoreboard(player: Player, activate: Boolean): SimpleScoreboard {
        return scoreboards.getOrPut(player.uniqueId) {
            val board: SimpleScoreboard = ScoreboardLib.createScoreboard(player)
            board.handler = this
            board.updateInterval = 10
            board.update()
            if (activate) {
                board.activate()
            }
            return board
        }
    }

    override fun getTitle(player: Player): String {
        return "${ChatColor.YELLOW}${ChatColor.BOLD}BED WARS"
    }

    override fun getEntries(player: Player): List<Entry>? {
        val game = plugin.gameManager.getGame(player) ?: return null
        val entry = EntryBuilder()
        entry.blank()
        if (game.state == GameState.LOBBY || game.state == GameState.STARTING) {
            entry.next("Map: ${ChatColor.GREEN}${game.data.world.name}")
                .next("Players: ${ChatColor.GREEN}${game.playersInLobby.size}/${game.data.maxPlayers}")
            if (game.state == GameState.STARTING) {
                entry.blank()
                    .next("Starting in ${ChatColor.GREEN}${game.countdown + 1}s")
            }
            return entry.blank()
                .next("Min Players: ${ChatColor.GREEN}${game.data.minPlayers}")
                .blank()
                .next("Max Players: ${ChatColor.GREEN}${game.data.maxPlayers}")
                .blank()
                .build()
        }
        if (game.state != GameState.STARTED) {
            return entry.build()
        }
        entry.next("Time: ${ChatColor.GREEN}${game.getElapsedTime().formatTime()}")
            .blank()
        val time = System.currentTimeMillis()
        val start = game.time
        val tiers = game.spawnerManager.upgradeLevels
        var closest: Pair<SpawnerType, Delay>? = null
        for (spawner in game.data.spawners.map(SpawnerData::type)) {
            val next = tiers.getOrDefault(spawner, 1) + 1
            if (next > 3) {
                continue
            }
            val timeUntil = Delay.fromMillis(start) + when (next) {
                2 -> spawner.secondTime ?: continue
                3 -> spawner.thirdTime ?: continue
                else -> continue
            } - Delay.fromMillis(time)
            if (closest == null) {
                closest = spawner to timeUntil
                continue
            }
            val delay = closest.second
            if (delay > timeUntil) {
                closest = spawner to timeUntil
            }
        }
        if (closest != null) {
            val spawner = closest.first
            val delay = closest.second
            val tier = (tiers.getOrDefault(spawner, 1) + 1).toRomanNumeral()
            entry.next("${StringUtils.capitalize(closest.first.name.lowercase())} $tier in${ChatColor.GREEN} ${delay.formatTime()}")
                .blank()
        }
        for (data in game.data.teams) {
            val team = data.team
            val stringBuilder = StringBuilder(team.chatColor.toString())
            stringBuilder.append(team.name[0].toString().uppercase())
                .append("${ChatColor.WHITE} ${team.displayName}:")
            if (game.getPlayersInTeam(team).isEmpty()) {
                stringBuilder.append("${ChatColor.RED}${ChatColor.BOLD} 〤")
            } else {
                val bed = game.beds[team]
                if (bed == null || bed == false) {
                    stringBuilder.append("${ChatColor.GREEN} ${game.getPlayersInTeam(team).size}")
                } else {
                    stringBuilder.append("${ChatColor.GREEN} ✔")
                }
            }
            if (game.getPlayersInTeam(team).contains(player.uniqueId)) {
                stringBuilder.append("${ChatColor.GRAY} YOU")
            }
            entry.next(stringBuilder.toString())
        }
        entry.blank()
        return entry.build()
    }
}