package me.dkim19375.bedwars.plugin.command

import com.google.common.collect.HashMultimap
import com.google.common.collect.Lists
import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.enumclass.Team
import me.dkim19375.bedwars.plugin.util.getMaxHelpPages
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil

class TabCompletionHandler(private val plugin: BedwarsPlugin) : TabCompleter {

    private val completesListMap: HashMultimap<String, String> = HashMultimap.create()

    init {
        add("core", "help", "list", "join", "quickjoin", "leave", "reload", "create", "delete", "save", "stop", "setup")
        add("spawners", "iron", "gold", "diamond", "emerald")
        add("colors", *Team.values().map(Team::displayName).toTypedArray())
        add("setup", "lobby", "spec", "minplayers", "maxplayers", "shop", "upgrades", "spawner", "team", "bed", "ready", "spec", "finish")
        add("addRemove", "add", "remove")
    }

    private fun add(key: String, vararg args: String) {
        completesListMap.putAll(key, listOf(*args))
    }

    private fun getPartial(token: String, collection: Iterable<String>): List<String> {
        return StringUtil.copyPartialMatches(token, collection, ArrayList())
    }

    private fun getHelpTab(sender: CommandSender): List<String> {
        return (1..sender.getMaxHelpPages()).map { i -> i.toString() }
    }

    private fun getTeams(player: CommandSender, gameName: String): List<String> {
        if (player !is Player) {
            return emptyList()
        }
        val game = plugin.gameManager.getGame(gameName) ?: return Team.values().map(Team::displayName)
        return game.data.teams.map { data -> data.team.displayName }
    }

    private fun getMissingTeams(sender: CommandSender, game: String): List<String> {
        val list = completesListMap["colors"].toMutableList()
        list.removeAll(getTeams(sender, game))
        return list
    }

    private fun getBedwarsGames(): List<String> {
        return plugin.gameManager.getGames().keys.toList()
    }

    override fun onTabComplete(sender: CommandSender, cmd: Command, label: String, args: Array<String>): List<String> {
        when (args.size) {
            0 -> return Lists.newArrayList(completesListMap["core"])
            1 -> return getPartial(args[0], completesListMap["core"])
            2 -> {
                return when (args[0].toLowerCase()) {
                    "help" -> getPartial(args[1], getHelpTab(sender))
                    "join" -> getPartial(args[1], getBedwarsGames())
                    "delete" -> getPartial(args[1], getBedwarsGames())
                    "save" -> getPartial(args[1], getBedwarsGames())
                    "stop" -> getPartial(args[1], getBedwarsGames())
                    "setup" -> getPartial(args[1], getBedwarsGames())
                    else -> emptyList()
                }
            }
            3 -> {
                if (!args[0].equals("setup", ignoreCase = true)) {
                    return emptyList()
                }
                return getPartial(args[2], completesListMap["setup"])
            }
            4 -> {
                if (!args[0].equals("setup", ignoreCase = true)) {
                    return emptyList()
                }
                return when (args[2].toLowerCase()) {
                    "shop" -> getPartial(args[3], completesListMap["addRemove"])
                    "upgrades" -> getPartial(args[3], completesListMap["addRemove"])
                    "spawner" -> getPartial(args[3], completesListMap["addRemove"])
                    "team" -> getPartial(args[3], completesListMap["addRemove"])
                    "bed" -> getPartial(args[3], completesListMap["addRemove"])
                    else -> emptyList()
                }
            }
            5 -> {
                if (!args[0].equals("setup", ignoreCase = true)) {
                    return emptyList()
                }
                return when (args[2].toLowerCase()) {
                    "spawner" -> {
                        return when (args[3].toLowerCase()) {
                            "remove" -> emptyList()
                            "add" -> getPartial(args[4], completesListMap["spawners"])
                            else -> emptyList()
                        }
                    }
                    "team" -> {
                        return when (args[3].toLowerCase()) {
                            "add" -> getMissingTeams(sender, args[1])
                            "remove" -> getPartial(args[4], getTeams(sender, args[1]))
                            else -> emptyList()
                        }
                    }
                    "bed" -> {
                        return when (args[3].toLowerCase()) {
                            "add" -> getMissingTeams(sender, args[1])
                            "remove" -> getPartial(args[4], getTeams(sender, args[1]))
                            else -> emptyList()
                        }
                    }
                    else -> emptyList()
                }
            }
            else -> return emptyList()
        }
    }
}