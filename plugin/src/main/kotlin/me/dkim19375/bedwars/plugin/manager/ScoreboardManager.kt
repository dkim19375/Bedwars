package me.dkim19375.bedwars.plugin.manager

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.enumclass.GameState
import me.dkim19375.bedwars.plugin.enumclass.formatText
import me.dkim19375.bedwars.plugin.util.Delay
import me.dkim19375.bedwars.plugin.util.formatTime
import me.tigerhix.lib.scoreboard.ScoreboardLib
import me.tigerhix.lib.scoreboard.common.EntryBuilder
import me.tigerhix.lib.scoreboard.type.Entry
import me.tigerhix.lib.scoreboard.type.Scoreboard
import me.tigerhix.lib.scoreboard.type.ScoreboardHandler
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import java.util.*

class ScoreboardManager(private val plugin: BedwarsPlugin) : ScoreboardHandler, Listener {
    private val scoreboards = mutableMapOf<UUID, Scoreboard>()

    fun getScoreboard(player: Player): Scoreboard {
        return scoreboards.getOrPut(player.uniqueId) {
            val board = ScoreboardLib.createScoreboard(player)
            board.handler = this
            board.updateInterval = 1L
            return board
        }
    }

    override fun getTitle(player: Player): String {
        plugin.gameManager.getGame(player) ?: return ""
        return "${ChatColor.YELLOW}${ChatColor.BOLD}BED WARS"
    }

    override fun getEntries(player: Player): List<Entry>? {
        val game = plugin.gameManager.getGame(player) ?: return null
        val entry = EntryBuilder()
        entry.blank()
        if (game.state == GameState.LOBBY || game.state == GameState.STARTING) {
            entry.next("Map: ${ChatColor.GREEN}${game.data.world.name}")
                .next("Players: ${ChatColor.GREEN}${game.playersInLobby.size}/${game.data.maxPlayers}")
            if (game.task != null) {
                entry.blank()
                    .next("Starting in ${ChatColor.GREEN}${game.countdown / 20}s")
            }
            return entry.blank()
                .next("Min Players: ${ChatColor.GREEN}${game.data.minPlayers}")
                .blank()
                .build()
        }
        if (game.state != GameState.STARTED) {
            return entry.build()
        }
        entry.next("Time: ${ChatColor.GREEN}${Delay.fromTime(game.time).seconds.formatTime()}")
            .blank()
        for (data in game.data.teams) {
            val team = data.team
            val stringBuilder = StringBuilder(team.color.formatText(team.name[0].toString().toUpperCase()))
            stringBuilder.append(" ${ChatColor.WHITE}${team.displayName}: ")
            if (game.getPlayersInTeam(team).isEmpty()) {
                stringBuilder.append("${ChatColor.RED}\u274C")
            } else {
                val bed = game.beds[team]
                if (bed == null || bed == false) {
                    stringBuilder.append("${ChatColor.RED}${game.getPlayersInTeam(team)}")
                } else {
                    stringBuilder.append("${ChatColor.GREEN}\u2714")
                }
            }
            if (game.getPlayersInTeam(team).contains(player.uniqueId)) {
                stringBuilder.append(" ${ChatColor.GRAY}YOU")
            }
        }
        return entry.build()
    }
}