package me.dkim19375.bedwars.plugin.manager

import io.github.thatkawaiisam.assemble.AssembleAdapter
import io.github.thatkawaiisam.assemble.events.AssembleBoardCreateEvent
import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.enumclass.GameState
import me.dkim19375.bedwars.plugin.enumclass.formatText
import me.dkim19375.bedwars.plugin.util.Delay
import me.dkim19375.bedwars.plugin.util.formatTime
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class ScoreboardManager(private val plugin: BedwarsPlugin) : AssembleAdapter, Listener {

    override fun getTitle(player: Player): String {
        plugin.gameManager.getGame(player)?: return ""
        return "${ChatColor.YELLOW}${ChatColor.BOLD}BED WARS"
    }

    override fun getLines(player: Player): MutableList<String> {
        val game = plugin.gameManager.getGame(player)?: return mutableListOf()
        val list = mutableListOf<String>()
        list.add(" ")
        if (game.state == GameState.LOBBY || game.state == GameState.STARTING) {
            list.add("Map: ${ChatColor.GREEN}${game.data.displayName}")
            list.add("Players: ${ChatColor.GREEN}${game.playersInLobby.size}/${game.data.maxPlayers}")
            if (game.task != null) {
                list.add(" ")
                list.add("Starting in ${ChatColor.GREEN}${game.countdown / 20}s")
            }
            list.add(" ")
            list.add("Min Players: ${ChatColor.GREEN}${game.data.minPlayers}")
            list.add(" ")
            return list
        }
        if (game.state != GameState.STARTED) return list
        list.add("Time: ${ChatColor.GREEN}${Delay.fromTime(game.time).seconds.formatTime()}")
        list.add(" ")
        for (team in game.data.teams.keys) {
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
        return list
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun AssembleBoardCreateEvent.onCreate() {
        val game = plugin.gameManager.getGame(player)
        if (game == null) {
            isCancelled = true
        }
    }
}