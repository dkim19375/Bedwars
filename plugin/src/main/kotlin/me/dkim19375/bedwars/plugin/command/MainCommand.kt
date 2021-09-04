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

import me.dkim19375.bedwars.api.enumclass.GameState
import me.dkim19375.bedwars.api.enumclass.Result
import me.dkim19375.bedwars.api.enumclass.Team
import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.builder.DataEditor
import me.dkim19375.bedwars.plugin.data.BedData
import me.dkim19375.bedwars.plugin.data.SpawnerData
import me.dkim19375.bedwars.plugin.data.TeamData
import me.dkim19375.bedwars.plugin.enumclass.ErrorMessages
import me.dkim19375.bedwars.plugin.enumclass.Permissions
import me.dkim19375.bedwars.plugin.enumclass.SpawnerType
import me.dkim19375.bedwars.plugin.manager.BedwarsGame
import me.dkim19375.bedwars.plugin.util.*
import me.dkim19375.dkimbukkitcore.data.toWrapper
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import java.util.*
import kotlin.system.measureTimeMillis


class MainCommand(private val plugin: BedwarsPlugin) : CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): Boolean {
        if (!sender.hasPermission(Permissions.COMMAND)) {
            sender.sendMessage(ErrorMessages.NO_PERMISSION)
            return true
        }
        if (args.isEmpty()) {
            sender.showHelpMsg(label)
            return true
        }
        when (args[0].lowercase()) {
            "help" -> {
                if (args.size > 1) {
                    sender.showHelpMsg(
                        label,
                        if (args[1].toIntOrNull() == null) 1 else args[1].toInt()
                    )
                    return true
                }
                sender.showHelpMsg(label)
                return true
            }
            "list" -> {
                if (!sender.hasPermission(Permissions.LIST)) {
                    sender.sendMessage(ErrorMessages.NO_PERMISSION)
                    return true
                }
                sender.sendMessage("${ChatColor.GREEN}Bedwars Games:")
                for (gameEntry in plugin.gameManager.getGames()) {
                    val name = gameEntry.key
                    val game = gameEntry.value
                    sender.sendMessage(
                        "${ChatColor.AQUA}- $name: ${
                            when (game.state) {
                                GameState.LOBBY -> "${ChatColor.GREEN}Ready"
                                GameState.STARTING -> "${ChatColor.RED}Starting"
                                GameState.STARTED -> "${ChatColor.YELLOW}Running"
                                GameState.STOPPED -> "${ChatColor.RED}Stopped"
                                GameState.REGENERATING_WORLD -> "${ChatColor.RED}Restarting"
                                GameState.GAME_END -> "${ChatColor.RED}Game Ended"
                            }
                        }"
                    )
                }
                return true
            }
            "join" -> {
                if (!check(sender, args, 2, Permissions.JOIN, true)) {
                    return true
                }
                val player = sender as Player
                val game = plugin.gameManager.getGame(args[1])
                if (game == null) {
                    player.sendMessage(ErrorMessages.INVALID_GAME)
                    return true
                }
                when (val result = game.addPlayer(player)) {
                    Result.SUCCESS -> {
                        player.sendMessage("${ChatColor.GREEN}Successfully joined the bedwars game!")
                        return true
                    }
                    Result.GAME_RUNNING -> {
                        player.sendMessage("${ChatColor.RED}The game is already running!")
                        return true
                    }
                    Result.GAME_STOPPED -> {
                        player.sendMessage("${ChatColor.RED}The game is not running!")
                        return true
                    }
                    Result.TOO_MANY_PLAYERS -> {
                        player.sendMessage("${ChatColor.RED}The game is full!")
                        return true
                    }
                    else -> {
                        player.sendMessage("${ChatColor.RED}An error has occurred: ${result.message}")
                        return true
                    }
                }
            }
            "quickjoin" -> {
                if (!check(sender, args, 1, Permissions.JOIN, true)) {
                    return true
                }
                val player = sender as Player
                var maxGame: Pair<BedwarsGame, Int>? = null
                for (game in plugin.gameManager.getGames().values) {
                    if (game.state != GameState.LOBBY && game.state != GameState.STARTING) {
                        continue
                    }
                    if ((game.getPlayersInGame().size + 1) >= game.data.maxPlayers) {
                        continue
                    }
                    if (maxGame == null) {
                        maxGame = Pair(game, game.data.maxPlayers - game.getPlayersInGame().size)
                        continue
                    }
                    val other = maxGame.second
                    if (other < game.data.maxPlayers - game.getPlayersInGame().size) {
                        maxGame = Pair(game, game.data.maxPlayers - game.getPlayersInGame().size)
                    }
                }
                if (maxGame == null) {
                    player.sendMessage("${ChatColor.RED}Could not find any games!")
                    return true
                }
                player.sendMessage("${ChatColor.GREEN}Successfully found a game: ${maxGame.first.data.world.name}!")
                maxGame.first.addPlayer(player)
                return true
            }
            "leave" -> {
                if (!check(sender, args, 1, Permissions.LEAVE, true)) {
                    return true
                }
                val player = sender as Player
                val game = plugin.gameManager.getGame(player)
                if (game == null) {
                    player.sendMessage("${ChatColor.RED}You are not in a game!")
                    return true
                }
                game.leavePlayer(player)
                player.sendMessage("${ChatColor.GREEN}Successfully left the game!")
                return true
            }
            "reload" -> {
                if (!sender.hasPermission(Permissions.RELOAD)) {
                    sender.sendMessage(ErrorMessages.NO_PERMISSION)
                    return true
                }
                sender.sendMessage("${ChatColor.YELLOW}Reloading...")
                val time = measureTimeMillis(plugin::reloadConfig)
                sender.sendMessage("${ChatColor.GREEN}Successfully reloaded in ${time}ms!")
                return true
            }
            "create" -> {
                if (!check(sender, args, 1, Permissions.SETUP, true)) {
                    return true
                }
                val player = sender as Player
                val world = player.world
                val worldName = world.name
                if (plugin.gameManager.getGame(worldName) != null) {
                    player.sendMessage(ErrorMessages.GAME_ALREADY_EXISTS)
                    return true
                }
                if (plugin.gameManager.builders.keys.containsIgnoreCase(worldName)) {
                    player.sendMessage(ErrorMessages.GAME_ALREADY_EXISTS)
                    return true
                }
                val builder = DataEditor(plugin, null, null, world).save()
                plugin.gameManager.builders[worldName] = builder
                player.sendMessage("${ChatColor.GREEN}Successfully created a game!")
                return true
            }
            "delete" -> {
                val game = hasPermissionAndValidGame(sender, args, mustBePlayer = false) ?: return true
                game.broadcast("${ChatColor.RED}Game force stopping!")
                game.forceStop()
                plugin.gameManager.deleteGame(game)
                sender.sendMessage("${ChatColor.GREEN}Successfully deleted the game!")
                return true
            }
            "save" -> {
                val game = hasPermissionAndValidGame(sender, args, false)
                val player = sender as? Player
                if (game == null) {
                    player ?: run {
                        sender.sendMessage(ErrorMessages.MUST_BE_PLAYER)
                        return true
                    }
                    val editor = getEditorFromWorld(args.getOrNull(1) ?: player.world.name, player) ?: return true
                    val gameData = editor.data.build()
                    if (gameData == null) {
                        player.sendMessage(
                            "${ChatColor.RED}Could not create the game! " +
                                    "Do ${ChatColor.GOLD}/$label setup ${editor.data.world.name} ready ${ChatColor.RED}for more information!"
                        )
                        return true
                    }
                    player.sendMessage("${ChatColor.GREEN}Saving the data...")
                    gameData.save(plugin)
                    val newGame = BedwarsGame(plugin, gameData)
                    newGame.saveMap()
                    newGame.state = GameState.LOBBY
                    plugin.dataFileManager.setEditing(gameData, false)
                    plugin.gameManager.builders.remove(gameData.world.name)
                    plugin.gameManager.addGame(newGame)
                    player.sendMessage("${ChatColor.GREEN}Successfully saved! (Map: ${newGame.data.world.name})")
                    return true
                }
                player?.let {
                    val worldManager = plugin.worldManager
                    if (worldManager != null) {
                        worldManager.removePlayersFromWorld(game.data.world.name)
                        return@let
                    }
                }
                game.saveMap()
                plugin.dataFileManager.setEditing(game.data, false)
                game.state = GameState.LOBBY
                sender.sendMessage("${ChatColor.GREEN}Successfully saved!")
                return true
            }
            "stop" -> {
                if (!check(sender, args, 1, Permissions.STOP, false)) {
                    return true
                }
                if (args.getOrNull(1)?.equals("all", ignoreCase = true) == true) {
                    val games = plugin.gameManager.getRunningGames().values
                    val amount = games.size
                    if (amount <= 0) {
                        sender.sendMessage("${ChatColor.GREEN}Successfully stopped $amount games in 0 s!")
                        return true
                    }
                    val start = System.currentTimeMillis()
                    for ((count, game) in games.toList().withIndex()) {
                        game.broadcast("${ChatColor.RED}An admin has force stopped the game!")
                        sender.sendMessage("${ChatColor.GREEN}Stopping the game..")
                        val time = System.currentTimeMillis()
                        game.forceStop {
                            sender.sendMessage(
                                "${ChatColor.GREEN}Successfully stopped game ${game.data.world.name} " +
                                        "(${count + 1}/$amount) (${System.currentTimeMillis() - time}ms)!"
                            )
                            if ((count + 1) >= amount) {
                                sender.sendMessage("${ChatColor.GREEN}Successfully stopped $amount games in ${System.currentTimeMillis() - start}ms!")
                            }
                        }
                    }
                    return true
                }
                val game = (args.getOrNull(1) ?: (sender as? Player)?.world?.name)?.let(plugin.gameManager::getGame)
                if (game == null) {
                    sender.sendMessage(ErrorMessages.INVALID_GAME)
                    return true
                }
                game.broadcast("${ChatColor.RED}An admin has force stopped the game!")
                sender.sendMessage("${ChatColor.GREEN}Stopping the game..")
                val time = System.currentTimeMillis()
                game.forceStop {
                    sender.sendMessage("${ChatColor.GREEN}Successfully stopped the game (${System.currentTimeMillis() - time}ms)!")
                }
                return true
            }
            "lobby" -> {
                if (!sender.hasPermission(Permissions.SETUP)) {
                    sender.sendMessage(ErrorMessages.NO_PERMISSION)
                    return true
                }
                if (sender !is Player) {
                    sender.sendMessage(ErrorMessages.MUST_BE_PLAYER)
                    return true
                }
                if (args.getOrNull(1)?.contains("disable", true) == true) {
                    plugin.dataFileManager.setLobby(null)
                    sender.sendMessage("${ChatColor.GREEN}Successfully disabled the lobby!")
                    return true
                }
                plugin.dataFileManager.setLobby(sender.location.clone())
                sender.sendMessage("${ChatColor.GREEN}Successfully set the lobby!")
                return true
            }
            "start" -> {
                if (!check(sender, args, 1, Permissions.START, false)) {
                    return true
                }
                val game = (args.getOrNull(1) ?: (sender as? Player)?.world?.name)?.let(plugin.gameManager::getGame)
                if (game == null) {
                    sender.sendMessage(ErrorMessages.INVALID_GAME)
                    return true
                }
                val result = game.start(true)
                if (result == Result.SUCCESS) {
                    sender.sendMessage("${ChatColor.GREEN}Successfully started the game!")
                    return true
                }
                sender.sendMessage("${ChatColor.RED}Error while starting the game: ${result.message}")
                return true
            }
            "edit" -> {
                val game = hasPermissionAndValidGame(sender, args, editing = true) ?: return true
                if (game.state != GameState.LOBBY) {
                    sender.sendMessage("${ChatColor.RED}The game is still running! do /$label stop ${game.data.world.name} to stop it!")
                    return true
                }
                if (plugin.dataFileManager.isEditing(game.data)) {
                    sender.sendMessage("${ChatColor.RED}The game is already in edit mode!")
                    return true
                }
                plugin.dataFileManager.setEditing(game.data, true)
                game.state = GameState.STOPPED
                sender.sendMessage(
                    "${ChatColor.GREEN}Successfully set the game mode to edit! " +
                            "Do ${ChatColor.GOLD}/$label save ${game.data.world.name} ${ChatColor.GREEN}to save it!"
                )
                if (sender !is Player) {
                    return true
                }
                return true
            }
            "info" -> {
                if (!check(sender, args, 2, Permissions.INFO, false)) {
                    return true
                }
                val world = Bukkit.getWorld(args[1])
                if (world == null) {
                    sender.sendMessage(ErrorMessages.INVALID_WORLD)
                    return true
                }
                val editor = DataEditor.findFromWorld(world, plugin)
                if (editor == null) {
                    sender.sendMessage(ErrorMessages.INVALID_GAME)
                    return true
                }
                val data = editor.data
                sender.sendMessage("${ChatColor.DARK_BLUE}------------------------------------------------")
                sender.sendMessage("${ChatColor.AQUA}World name: ${ChatColor.GOLD}${data.world.name}")
                sender.sendMessage("${ChatColor.AQUA}Minimum players: ${ChatColor.GOLD}${data.minPlayers}")
                sender.sendMessage("${ChatColor.AQUA}Maximum players: ${ChatColor.GOLD}${data.maxPlayers}")
                sender.sendMessage("${ChatColor.AQUA}Lobby: ${ChatColor.GOLD}${
                    data.lobby?.toWrapper()?.format() ?: "None"
                }")
                sender.sendMessage("${ChatColor.AQUA}Spectator location: ${ChatColor.GOLD}${
                    data.spec?.toWrapper()?.format() ?: "None"
                }")
                sender.sendMessage(
                    "${ChatColor.AQUA}Teams: ${ChatColor.GOLD}\n- ${
                        if (data.teams.isEmpty()) "None" else {
                            data.teams.joinToString("\n- ") {
                                    d,
                                ->
                                "team: ${d.team.displayName}, spawn: ${d.spawn.toWrapper().format()}"
                            }
                        }
                    }"
                )
                sender.sendMessage(
                    "${ChatColor.AQUA}Beds: ${ChatColor.GOLD}\n- ${
                        if (data.beds.isEmpty()) "None" else {
                            data.beds.joinToString("\n- ") {
                                    d,
                                ->
                                "team: ${d.team.displayName}, location: ${d.location.toWrapper().format()}"
                            }
                        }
                    }"
                )
                sender.sendMessage(
                    "${ChatColor.AQUA}Spawners: ${ChatColor.GOLD}\n- ${
                        if (data.spawners.isEmpty()) "None" else {
                            data.spawners.joinToString("\n- ") {
                                    d,
                                ->
                                "type: ${d.type.material.name.capAndFormat()}, location: ${
                                    d.location.toWrapper().format()
                                }"
                            }
                        }
                    }"
                )
                sender.sendMessage("${ChatColor.AQUA}Shop villagers: ${ChatColor.GOLD}${data.shopVillagers.size}")
                sender.sendMessage("${ChatColor.AQUA}Upgrade villagers: ${ChatColor.GOLD}${data.upgradeVillagers.size}")
                sender.sendMessage("${ChatColor.AQUA}Can build: ${ChatColor.GOLD}${
                    data.canBuild().isEmpty().getGreenOrRed()
                }")
                sender.sendMessage("${ChatColor.DARK_BLUE}------------------------------------------------")
                return true
            }
            "setup" -> {
                if (!check(sender, args, 2, Permissions.SETUP, false)) {
                    return true
                }
                val newArgs: List<String>
                val editor = getEditorFromWorld(args[1], sender, sender !is Player).also {
                    newArgs = args.drop(if (it == null) 1 else 2)
                } ?: (sender as? Player)?.let { getEditorFromWorld(it.world.name, it) } ?: return true
                when (newArgs[0].lowercase()) {
                    "ready" -> {
                        val buildStatus = editor.data.canBuild()
                        if (buildStatus.isEmpty()) {
                            sender.sendMessage("${ChatColor.GREEN}The game is ready to be saved!")
                            return true
                        }
                        sender.sendMessage("${ChatColor.RED}There are ${buildStatus.size} errors!")
                        for (error in buildStatus) {
                            sender.sendMessage("${ChatColor.GOLD}- ${error.message}")
                        }
                        return true
                    }
                    "lobby" -> {
                        if (sender !is Player) {
                            sender.sendMessage(ErrorMessages.MUST_BE_PLAYER)
                            return true
                        }
                        editor.data.lobby = sender.location
                        editor.save()
                        sender.sendMessage("${ChatColor.GREEN}Successfully set the lobby!")
                        return true
                    }
                    "spec" -> {
                        if (sender !is Player) {
                            sender.sendMessage(ErrorMessages.MUST_BE_PLAYER)
                            return true
                        }
                        editor.data.spec = sender.location
                        editor.save()
                        sender.sendMessage("${ChatColor.GREEN}Successfully set the spectator spawn!")
                        return true
                    }
                    "minplayers" -> {
                        if (newArgs.size < 2) {
                            sender.sendMessage("${ChatColor.GOLD}Minimum Players: ${editor.data.minPlayers}")
                            return true
                        }
                        val min = newArgs[1].toIntOrNull()
                        if (min == null) {
                            sender.sendMessage("${ChatColor.RED}'${ChatColor.GOLD}${newArgs[1]}${ChatColor.RED}' isn't a valid number!")
                            return true
                        }
                        if (min > editor.data.maxPlayers) {
                            sender.sendMessage(
                                "${ChatColor.RED}The minimum (${ChatColor.GOLD}$min${ChatColor.RED}) " +
                                        "cannot be more than the maximum (${ChatColor.GOLD}${editor.data.maxPlayers}${ChatColor.RED}!"
                            )
                            return true
                        }
                        editor.data.minPlayers = min
                        editor.save()
                        sender.sendMessage("${ChatColor.GREEN}Successfully set the minimum players to ${ChatColor.GOLD}$min${ChatColor.GREEN}!")
                        return true
                    }
                    "maxplayers" -> {
                        if (newArgs.size < 2) {
                            sender.sendMessage("${ChatColor.GOLD}Maximum Players: ${editor.data.maxPlayers}")
                            return true
                        }
                        val max = newArgs[1].toIntOrNull()
                        if (max == null) {
                            sender.sendMessage("${ChatColor.RED}'${ChatColor.GOLD}${newArgs[1]}${ChatColor.RED}' isn't a valid number!")
                            return true
                        }
                        if (max < editor.data.minPlayers) {
                            sender.sendMessage(
                                "${ChatColor.RED}The maximum (${ChatColor.GOLD}$max${ChatColor.RED}) " +
                                        "cannot be less than the minimum (${ChatColor.GOLD}${editor.data.minPlayers}${ChatColor.RED}!"
                            )
                            return true
                        }
                        editor.data.maxPlayers = max
                        editor.save()
                        sender.sendMessage("${ChatColor.GREEN}Successfully set the maximum players to ${ChatColor.GOLD}$max${ChatColor.GREEN}!")
                        return true
                    }
                    "shop" -> {
                        if (sender !is Player) {
                            sender.sendMessage(ErrorMessages.MUST_BE_PLAYER)
                            return true
                        }
                        shopCommand(sender, editor, newArgs, editor.data.shopVillagers, "shop")
                        return true
                    }
                    "upgrades" -> {
                        if (sender !is Player) {
                            sender.sendMessage(ErrorMessages.MUST_BE_PLAYER)
                            return true
                        }
                        shopCommand(sender, editor, newArgs, editor.data.upgradeVillagers, "upgrades")
                        return true
                    }
                    "spawner" -> {
                        if (newArgs.size < 2) {
                            sender.sendMessage(ErrorMessages.TOO_LITTLE_ARGS)
                            return true
                        }
                        if (sender !is Player) {
                            sender.sendMessage(ErrorMessages.MUST_BE_PLAYER)
                            return true
                        }
                        if (newArgs[1].equals("add", ignoreCase = true)) {
                            if (newArgs.size < 3) {
                                sender.sendMessage(ErrorMessages.TOO_LITTLE_ARGS)
                                return true
                            }
                            val type = SpawnerType.fromString(newArgs[2])
                            if (type == null) {
                                sender.sendMessage(ErrorMessages.INVALID_ARG)
                                return true
                            }
                            editor.data.spawners.add(SpawnerData(type, sender.location))
                            sender.sendMessage("${ChatColor.GREEN}Successfully created a spawner!")
                            editor.save()
                            return true
                        }
                        if (!newArgs[1].equals("remove", ignoreCase = true)) {
                            sender.sendMessage(ErrorMessages.INVALID_ARG)
                            return true
                        }
                        var closestSpawner: Pair<SpawnerData, Double>? = null
                        for (spawner in editor.data.spawners) {
                            val distance = spawner.location.getSafeDistance(sender.location)
                            if (distance > 5) {
                                continue
                            }
                            if (closestSpawner == null) {
                                closestSpawner = Pair(spawner, distance)
                                continue
                            }
                            if (distance < closestSpawner.second) {
                                closestSpawner = Pair(spawner, distance)
                            }
                        }
                        if (closestSpawner == null) {
                            sender.sendMessage("${ChatColor.RED}There are no nearby spawners!")
                            return true
                        }
                        val spawner = closestSpawner.first
                        editor.data.spawners.remove(spawner)
                        sender.sendMessage("${ChatColor.GREEN}Successfully removed a ${spawner.type.name.capAndFormat()} spawner!")
                        editor.save()
                        return true
                    }
                    "team" -> {
                        if (newArgs.size < 2) {
                            sender.sendMessage("${ChatColor.GREEN}Teams:")
                            for (teamData in editor.data.teams) {
                                val team = teamData.team
                                val bed = editor.data.beds.firstOrNull { d -> d.team == team }
                                sender.sendMessage(
                                    "${team.chatColor}${team.displayName}${ChatColor.GOLD}: " +
                                            "Is bed set: ${(bed != null).getGreenOrRed()}" +
                                            if (bed != null) "Yes" else {
                                                "No"
                                            }
                                )
                            }
                            return true
                        }
                        if (sender !is Player) {
                            sender.sendMessage(ErrorMessages.MUST_BE_PLAYER)
                            return true
                        }
                        if (newArgs.size < 3) {
                            sender.sendMessage(ErrorMessages.TOO_LITTLE_ARGS)
                            return true
                        }
                        if (newArgs[1].equals("add", ignoreCase = true)) {
                            val location = sender.location
                            val team = Team.fromString(newArgs[2])
                            if (team == null) {
                                sender.sendMessage(ErrorMessages.INVALID_TEAM)
                                return true
                            }
                            if (editor.data.teams.containsTeam(team)) {
                                sender.sendMessage("${ChatColor.RED}The team already exists!")
                                return true
                            }
                            editor.data.teams.setData(TeamData(team, location))
                            editor.save()
                            sender.sendMessage(
                                "${ChatColor.GREEN}Successfully created the team ${team.chatColor}${team.displayName.capAndFormat()}${ChatColor.GREEN}!"
                            )
                            return true
                        }
                        val team = getTeam(sender, newArgs) ?: return true
                        if (!editor.data.teams.containsTeam(team)) {
                            sender.sendMessage("${ChatColor.RED}The team doesn't exist!")
                            return true
                        }
                        editor.data.teams.removeTeam(team)
                        editor.save()
                        sender.sendMessage(
                            "${ChatColor.GREEN}Successfully removed the team ${team.chatColor}${
                                team.displayName.capAndFormat()
                            }${ChatColor.GREEN}!"
                        )
                        return true
                    }
                    "bed" -> {
                        if (sender !is Player) {
                            sender.sendMessage(ErrorMessages.MUST_BE_PLAYER)
                            return true
                        }
                        if (newArgs.size < 3) {
                            sender.sendMessage(ErrorMessages.TOO_LITTLE_ARGS)
                            return true
                        }
                        if (newArgs[1].equals("add", ignoreCase = true)) {
                            val team = Team.fromString(newArgs[2])
                            if (team == null) {
                                sender.sendMessage(ErrorMessages.INVALID_TEAM)
                                return true
                            }
                            val block = sender.location.block
                            if (!block.type.isBed()) {
                                sender.sendMessage("${ChatColor.RED}You are not standing on a bed!")
                                return true
                            }
                            for (bed in editor.data.beds) {
                                if (bed.team == team) {
                                    sender.sendMessage("${ChatColor.RED}A bed for that team already exists!")
                                    return true
                                }
                                if (bed.location.toWrapper() == block.getBedHead().toWrapper()) {
                                    sender.sendMessage("${ChatColor.RED}A bed for that team already exists!")
                                    return true
                                }
                            }
                            editor.data.beds.add(BedData.getBedData(team, block))
                            sender.sendMessage("${ChatColor.GREEN}Successfully set the bed!")
                            editor.save()
                            return true
                        }
                        val team = getTeam(sender, newArgs) ?: return true
                        if (editor.data.beds.none { data -> data.team == team }) {
                            sender.sendMessage("${ChatColor.RED}The team doesn't have a bed setup!")
                            return true
                        }
                        editor.data.beds.removeIf { data -> data.team == team }
                        editor.save()
                        sender.sendMessage("${ChatColor.GREEN}Successfully removed the bed!")
                        return true
                    }
                    else -> {
                        sender.sendMessage(ErrorMessages.INVALID_ARG)
                        return true
                    }
                }
            }
            else -> {
                sender.sendMessage(ErrorMessages.INVALID_ARG)
                return true
            }
        }
    }

    private fun shopCommand(
        sender: Player,
        editor: DataEditor,
        args: List<String>,
        villagers: MutableSet<UUID>,
        type: String,
    ) {
        if (args.size < 2) {
            sender.sendMessage(ErrorMessages.TOO_LITTLE_ARGS)
            return
        }
        val entity = sender.getLookingAt()
        val allVillagers = editor.data.shopVillagers.plus(editor.data.upgradeVillagers)
        if (entity == null || entity.type != EntityType.VILLAGER) {
            sender.sendMessage("${ChatColor.RED}You are not looking at a villager close enough!")
            return
        }
        if (args[1].equals("add", ignoreCase = true)) {
            if (allVillagers.contains(entity.uniqueId)) {
                sender.sendMessage("${ChatColor.RED}That entity is already a shop/upgrades villager!")
                return
            }
            val teleport: Boolean = (args.getOrNull(2)?.equals("teleport", ignoreCase = true) ?: false ||
                    args.getOrNull(2)?.equals("tp", ignoreCase = true) ?: false)
            val loc = entity.location.clone()
            loc.yaw = sender.location.getOppositeYaw().yaw
            villagers.add(entity.uniqueId)
            entity.teleportUpdated(if (teleport) sender.location else loc)
            editor.save()
            for (villager in villagers) {
                val e = villager.getEntity() as? LivingEntity ?: continue
                e.removeAI()
            }
            plugin.gameManager.getGames().values.forEach { g -> g.npcManager.disableAI() }
            sender.sendMessage("${ChatColor.GREEN}Successfully added the $type villager!")
            editor.save()
            return
        }
        if (!args[1].equals("remove", ignoreCase = true)) {
            sender.sendMessage(ErrorMessages.INVALID_ARG)
            return
        }
        if (!villagers.contains(entity.uniqueId)) {
            sender.sendMessage("${ChatColor.RED}That entity is not a $type villager!")
            return
        }
        villagers.remove(entity.uniqueId)
        entity.addAI()
        sender.sendMessage("${ChatColor.GREEN}Successfully removed the $type villager!")
        editor.save()
    }

    private fun check(
        sender: CommandSender,
        args: Array<out String>,
        minArgs: Int,
        permission: Permissions,
        bePlayer: Boolean,
    ): Boolean {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(ErrorMessages.NO_PERMISSION)
            return false
        }
        if (sender !is Player && bePlayer) {
            sender.sendMessage(ErrorMessages.MUST_BE_PLAYER)
            return false
        }
        if (minArgs > args.size) {
            sender.sendMessage(ErrorMessages.TOO_LITTLE_ARGS)
            return false
        }
        return true
    }

    private fun hasPermissionAndValidGame(
        sender: CommandSender,
        args: Array<out String>,
        showMsgForGame: Boolean = true,
        editing: Boolean = false,
        mustBePlayer: Boolean = false,
    ): BedwarsGame? {
        val player = sender as? Player
        if (!check(
                sender = sender,
                args = args,
                minArgs = if (player == null) 2 else 1,
                permission = Permissions.SETUP,
                bePlayer = mustBePlayer
            )
        ) {
            return null
        }
        val game = plugin.gameManager.getGame(args.getOrNull(1), player)
        if (!showMsgForGame) {
            return game
        }
        if (game == null) {
            sender.sendMessage(ErrorMessages.INVALID_GAME)
            return null
        }
        if (!game.isEditing() && !editing) {
            sender.sendMessage(ErrorMessages.NOT_EDIT_MODE)
            return null
        }
        return game
    }

    private fun getEditorFromWorld(worldName: String, sender: CommandSender, error: Boolean = true): DataEditor? {
        val world = Bukkit.getWorld(worldName)
        if (world == null) {
            if (error) {
                sender.sendMessage(ErrorMessages.INVALID_WORLD)
            }
            return null
        }
        val editor = DataEditor.findFromWorld(world, plugin)
        if (editor == null) {
            if (error) {
                sender.sendMessage(ErrorMessages.INVALID_GAME)
            }
            return null
        }
        return editor
    }

    private fun getTeam(sender: CommandSender, args: List<String>): Team? {
        if (!args[1].equals("remove", ignoreCase = true)) {
            sender.sendMessage(ErrorMessages.INVALID_ARG)
            return null
        }
        val team = Team.fromString(args[2])
        if (team == null) {
            sender.sendMessage(ErrorMessages.INVALID_TEAM)
            return null
        }
        return team
    }
}