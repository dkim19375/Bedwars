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
import me.dkim19375.bedwars.api.enumclass.Team
import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.builder.DataEditor
import me.dkim19375.bedwars.plugin.enumclass.Permissions
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
        add("spawners", "iron", "gold", "diamond", "emerald")
        add("colors", Team.values().map(Team::getDisplayName))
        add(
            "setup", "ready", "lobby", "spec", "minplayers", "maxplayers", "shop", "upgrades", "spawner",
            "team", "bed"
        )
        add("addRemove", "add", "remove")
        add("tp", "tp", "teleport")
    }

    private fun add(key: String, vararg args: String) = completesListMap.putAll(key, listOf(*args))

    @Suppress("SameParameterValue")
    private fun add(key: String, args: List<String>) = completesListMap.putAll(key, args)

    private fun getPartial(token: String, collection: Iterable<String>): List<String> {
        return StringUtil.copyPartialMatches(token, collection, ArrayList())
    }

    private fun getHelpTab(sender: CommandSender): List<String> {
        return (1..sender.getMaxHelpPages()).map { i -> i.toString() }
    }

    private fun getTeams(gameName: String): List<String> {
        val game = getEditorFromWorld(gameName) ?: return completesListMap["colors"].toList()
        return game.data.teams.map { data -> data.team.displayName }
    }

    private fun getMissingTeams(game: String): List<String> {
        val list = completesListMap["colors"].toMutableList()
        list.removeAll(getTeams(game))
        return list
    }

    private fun getEditorFromWorld(worldName: String): DataEditor? {
        val world = Bukkit.getWorld(worldName) ?: return null
        return DataEditor.findFromWorld(world, plugin)
    }

    private fun getBedwarsGames(): List<String> = plugin.gameManager.getGames().keys.toList()

    private fun getAllGames(): List<String> = (getBedwarsGames() + plugin.gameManager.builders.keys).toSet().toList()

    private fun getPartialPerm(
        token: String,
        collection: Iterable<String>,
        sender: Permissible,
        perm: Permissions = Permissions.SETUP,
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
        if (sender.hasPermission(Permissions.STATISTICS)) {
            list.add("stats")
        }
        if (!sender.hasPermission(Permissions.SETUP)) {
            return list
        }
        list.add("create")
        list.add("delete")
        list.add("save")
        list.add("lobby")
        list.add("setup")
        list.add("edit")
        return list
    }

    private fun getSecondArg(sender: CommandSender, args: Array<String>): List<String> = if (getAllGames().none {
            it.startsWith(args[1], true)
        } && sender is Player) {
        completesListMap["setup"].toList()
    } else {
        getAllGames().plus(if (sender is Player && getAllGames().any { it.equals(sender.world.name, true) }) {
            completesListMap["setup"].toList()
        } else {
            emptyList()
        })
    }

    private fun getRestArgs(sender: CommandSender, args: Array<String>): Pair<String, List<String>> {
        return if (sender is Player && getEditorFromWorld(args[1]) == null) {
            sender.world.name to args.drop(1)
        } else {
            args[1] to args.drop(2)
        }
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
                    "save" -> getPartialPerm(args[1], getAllGames(), sender)
                    "setup" -> getPartialPerm(args[1], getSecondArg(sender, args), sender)
                    "start" -> getPartialPerm(args[1], getBedwarsGames(), sender, Permissions.START)
                    "stop" -> getPartialPerm(args[1], getBedwarsGames(), sender, Permissions.STOP)
                    "lobby" -> getPartialPerm(args[1], listOf("disable"), sender)
                    "info" -> getPartialPerm(args[1], getBedwarsGames(), sender, Permissions.INFO)
                    "stats" -> getPartialPerm(
                        token = args[1],
                        collection = Bukkit.getOnlinePlayers().map(Player::getName)
                            .toSet().plus(plugin.mainDataFile.get().nameCache.keys),
                        sender = sender,
                        perm = Permissions.STATISTICS
                    )
                    else -> emptyList()
                }
            }
        }
        if (!args[0].equals("setup", true) || !sender.hasPermission(Permissions.SETUP)) {
            return emptyList()
        }
        val restArgs = getRestArgs(sender, args)
        val worldName = restArgs.first
        val newArgs = restArgs.second
        when (newArgs.size) {
            1 -> {
                return getPartial(newArgs[0], completesListMap["setup"])
            }
            2 -> {
                val data = Bukkit.getWorld(worldName)?.let { DataEditor.findFromWorld(it, plugin) }?.data
                return when (newArgs[0].lowercase()) {
                    "minplayers" -> (data?.maxPlayers?.let { (1..it).map(Int::toString) })?.let {
                        getPartial(newArgs[1], it)
                    } ?: listOf("<min>")
                    "maxplayers" -> listOf("<max>")
                    "shop", "upgrades", "spawner",
                    "team", "bed",
                    -> getPartial(newArgs[1], completesListMap["addRemove"])
                    else -> emptyList()
                }
            }
            3 -> {
                return when (newArgs[0].lowercase()) {
                    "shop", "upgrades" -> {
                        if (!newArgs[1].equals("add", true)) {
                            return emptyList()
                        }
                        return getPartial(newArgs[2], completesListMap["tp"])
                    }
                    "spawner" -> {
                        return if (newArgs[1].lowercase() == "add") {
                            getPartial(newArgs[2], completesListMap["spawners"])
                        } else {
                            emptyList()
                        }
                    }
                    "team", "bed" -> {
                        return when (newArgs[1].lowercase()) {
                            "add" -> getPartial(newArgs[2], getMissingTeams(worldName))
                            "remove" -> getPartial(newArgs[2], getTeams(worldName))
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