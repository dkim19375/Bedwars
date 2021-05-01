package me.dkim19375.bedwars.plugin.manager

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.enumclass.GameState
import me.dkim19375.bedwars.plugin.util.formatTime
import me.dkim19375.bedwars.plugin.util.getPlayers
import me.tigerhix.lib.scoreboard.ScoreboardLib
import me.tigerhix.lib.scoreboard.common.EntryBuilder
import me.tigerhix.lib.scoreboard.type.Entry
import me.tigerhix.lib.scoreboard.type.ScoreboardHandler
import me.tigerhix.lib.scoreboard.type.SimpleScoreboard
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

    @Suppress("MemberVisibilityCanBePrivate")
    fun update(player: Player) {
        // getScoreboard(player, true).update()
    }

    fun update(game: BedwarsGame) {
        game.getPlayersInGame().getPlayers().forEach { p -> update(p) }
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
        for (data in game.data.teams) {
            val team = data.team
            val stringBuilder = StringBuilder(team.chatColor.toString())
            stringBuilder.append(team.name[0].toString().toUpperCase())
                .append(" ${ChatColor.WHITE}${team.displayName}: ")
            if (game.getPlayersInTeam(team).isEmpty()) {
                stringBuilder.append("${ChatColor.RED}${ChatColor.BOLD}\u3024")
            } else {
                val bed = game.beds[team]
                if (bed == null || bed == false) {
                    stringBuilder.append("${ChatColor.GREEN}${game.getPlayersInTeam(team).size}")
                } else {
                    stringBuilder.append("${ChatColor.GREEN}\u2714")
                }
            }
            if (game.getPlayersInTeam(team).contains(player.uniqueId)) {
                stringBuilder.append(" ${ChatColor.GRAY}YOU")
            }
            entry.next(stringBuilder.toString())
        }
        entry.blank()
        return entry.build()
    }
}