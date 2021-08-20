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

package me.dkim19375.bedwars.plugin.command

import com.google.common.collect.HashMultimap
import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.builder.DataEditor
import me.dkim19375.bedwars.plugin.enumclass.Permissions
import me.dkim19375.bedwars.plugin.enumclass.Team
import me.dkim19375.bedwars.plugin.util.getMaxHelpPages
import me.dkim19375.bedwars.plugin.util.hasPermission
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.permissions.Permissible
import org.bukkit.util.StringUtil

class TabCompletionHandler(private val plugin: BedwarsPlugin) : TabCompleter {

    private val completesListMap: HashMultimap<String, String> = HashMultimap.create()

    init {
        add(
            "core", "help", "list", "join", "quickjoin", "leave", "reload", "create", "delete", "save",
            "start", "stop", "edit", "info", "setup"
        )
        add("spawners", "iron", "gold", "diamond", "emerald")
        add("colors", *Team.values().map(Team::displayName).toTypedArray())
        add(
            "setup", "ready", "lobby", "spec", "minplayers", "maxplayers", "shop", "upgrades", "spawner",
            "team", "bed"
        )
        add("addRemove", "add", "remove")
        add("tp", "tp", "teleport")
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

    private fun getBedwarsGames(): List<String> = plugin.gameManager.getGames().keys.toList()

    private fun getAllGames(): List<String> = (getBedwarsGames() + plugin.gameManager.builders.keys).toSet().toList()

    private fun getPartialPerm(
        token: String,
        collection: Iterable<String>,
        sender: Permissible,
        perm: Permissions = Permissions.SETUP
    ): List<String>? {
        if (!sender.hasPermission(perm)) {
            return null
        }
        return getPartial(token, collection)
    }

    private fun getBaseCommands(sender: Permissible): List<String> {
        val list = mutableListOf("help")
        if (sender.hasPermission(Permissions.LIST)) {
            list.add("list")
        }
        if (sender.hasPermission(Permissions.JOIN)) {
            list.add("join")
            list.add("quickjoin")
        }
        if (sender.hasPermission(Permissions.LEAVE)) {
            list.add("leave")
        }
        if (sender.hasPermission(Permissions.RELOAD)) {
            list.add("reload")
        }
        if (sender.hasPermission(Permissions.START)) {
            list.add("start")
        }
        if (sender.hasPermission(Permissions.STOP)) {
            list.add("stop")
        }
        if (sender.hasPermission(Permissions.INFO)) {
            list.add("info")
        }
        if (!sender.hasPermission(Permissions.SETUP)) {
            return list
        }
        list.add("setup")
        list.add("save")
        list.add("edit")
        return list
    }

    override fun onTabComplete(sender: CommandSender, cmd: Command, label: String, args: Array<String>): List<String>? {
        if (!sender.hasPermission(Permissions.COMMAND)) {
            return null
        }
        when (args.size) {
            0 -> return getBaseCommands(sender)
            1 -> return getPartial(args[0], getBaseCommands(sender))
            2 -> {
                return when (args[0].lowercase()) {
                    "help" -> getPartial(args[1], getHelpTab(sender))
                    "join" -> getPartialPerm(args[1], getBedwarsGames(), sender, Permissions.JOIN)
                    "delete", "edit" -> getPartialPerm(args[1], getBedwarsGames(), sender)
                    "save", "setup" -> getPartialPerm(args[1], getAllGames(), sender)
                    "start" -> getPartialPerm(args[1], getBedwarsGames(), sender, Permissions.START)
                    "stop" -> getPartialPerm(args[1], getBedwarsGames(), sender, Permissions.STOP)
                    "info" -> getPartialPerm(args[1], getBedwarsGames(), sender, Permissions.INFO)
                    else -> emptyList()
                }
            }
            3 -> {
                if (!args[0].equals("setup", ignoreCase = true) || !sender.hasPermission(Permissions.SETUP)) {
                    return emptyList()
                }
                return getPartial(args[2], completesListMap["setup"])
            }
            4 -> {
                if (!args[0].equals("setup", ignoreCase = true) || !sender.hasPermission(Permissions.SETUP)) {
                    return emptyList()
                }
                val data = Bukkit.getWorld(args[1])?.let { DataEditor.findFromWorld(it, plugin) }?.data
                return when (args[2].lowercase()) {
                    "minplayers" -> (data?.maxPlayers?.let { (1..it).map(Int::toString) })?.let {
                        getPartial(args[3], it)
                    } ?: listOf("<min>")
                    "maxplayers" -> listOf("<max>")
                    "shop", "upgrades", "spawner",
                    "team", "bed" -> getPartial(args[3], completesListMap["addRemove"])
                    else -> emptyList()
                }
            }
            5 -> {
                if (!args[0].equals("setup", ignoreCase = true) || !sender.hasPermission(Permissions.SETUP)) {
                    return emptyList()
                }
                return when (args[2].lowercase()) {
                    "shop", "upgrades" -> {
                        if (!args[3].equals("add", true)) {
                            return emptyList()
                        }
                        return getPartial(args[4], completesListMap["tp"])
                    }
                    "spawner" -> {
                        return if (args[3].lowercase() == "add") {
                            getPartial(args[4], completesListMap["spawners"])
                        } else {
                            emptyList()
                        }
                    }
                    "team", "bed" -> {
                        return when (args[3].lowercase()) {
                            "add" -> getPartial(args[4], getMissingTeams(sender, args[1]))
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