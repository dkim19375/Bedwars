package me.dkim19375.bedwars.plugin.command

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.builder.DataEditor
import me.dkim19375.bedwars.plugin.data.BedData
import me.dkim19375.bedwars.plugin.data.SpawnerData
import me.dkim19375.bedwars.plugin.data.TeamData
import me.dkim19375.bedwars.plugin.enumclass.*
import me.dkim19375.bedwars.plugin.manager.BedwarsGame
import me.dkim19375.bedwars.plugin.util.*
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import java.util.*


class MainCommand(private val plugin: BedwarsPlugin) : CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (args.isEmpty()) {
            sender.showHelpMessage(label)
            return true
        }
        @Suppress("LiftReturnOrAssignment")
        when (args[0].toLowerCase()) {
            "debug" -> {
                if (sender !is Player) {
                    sender.sendMessage(ErrorMessages.MUST_BE_PLAYER)
                    return true
                }
                val game: BedwarsGame
                if (args.size > 1) {
                    val g = plugin.gameManager.getGame(args[1])
                    if (g == null) {
                        sender.sendMessage(ErrorMessages.INVALID_GAME)
                        return true
                    }
                    game = g
                } else {
                    val g = plugin.gameManager.getGame(sender)
                    if (g == null) {
                        sender.sendMessage(ErrorMessages.INVALID_GAME)
                        return true
                    }
                    game = g
                }
                sender.sendMessage(
                    "Is in lobby OR game: ${
                        if (game.playersInLobby.plus(game.players.values.getCombinedValues())
                                .contains(sender.uniqueId)
                        ) "Yes" else "No"
                    }"
                )
                sender.sendMessage("Game name: ${game.data.world.name}")
                sender.sendMessage("Players: ${game.getPlayersInGame().size}")
                sender.sendMessage("State: ${game.state.name}")
                sender.sendMessage(
                    "Player list: ${
                        game.getPlayersInGame().getPlayers().getUsernames().joinToString(", ")
                    }"
                )
                sender.sendMessage("Is in lobby: ${if (game.playersInLobby.contains(sender.uniqueId)) "Yes" else "No"}")
                sender.sendMessage(
                    "Is in game: ${
                        if (game.players.values.getCombinedValues().contains(sender.uniqueId)) "Yes" else "No"
                    }"
                )
                sender.sendMessage("Can start: ${game.canStart(false).name}, message: ${game.canStart(false).message}")
                return true
            }
            "help" -> {
                if (!sender.hasPermission(Permission.HELP)) {
                    sender.sendMessage(ErrorMessages.NO_PERMISSION)
                    return true
                }
                if (args.size > 1) {
                    sender.showHelpMessage(
                        label,
                        if (args[1].toIntOrNull() == null) 1 else args[1].toInt()
                    )
                    return true
                }
                sender.showHelpMessage(label)
                return true
            }
            "list" -> {
                if (!sender.hasPermission(Permission.LIST)) {
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
                            }
                        }"
                    )
                }
                return true
            }
            "join" -> {
                if (!sender.hasPermission(Permission.JOIN)) {
                    sender.sendMessage(ErrorMessages.NO_PERMISSION)
                    return true
                }
                if (sender !is Player) {
                    sender.sendMessage(ErrorMessages.MUST_BE_PLAYER)
                    return true
                }
                if (args.size < 2) {
                    sender.sendMessage(ErrorMessages.TOO_LITTLE_ARGS)
                    return true
                }
                val game = plugin.gameManager.getGame(args[1])
                if (game == null) {
                    sender.sendMessage(ErrorMessages.INVALID_GAME)
                    return true
                }
                when (game.addPlayer(sender)) {
                    BedwarsGame.Result.SUCCESS -> {
                        sender.sendMessage("${ChatColor.GREEN}Successfully joined the bedwars game!")
                        return true
                    }
                    BedwarsGame.Result.GAME_RUNNING -> {
                        sender.sendMessage("${ChatColor.RED}The game is already running!")
                        return true
                    }
                    BedwarsGame.Result.TOO_MANY_PLAYERS -> {
                        sender.sendMessage("${ChatColor.RED}The game is full!")
                        return true
                    }
                    else -> {
                        sender.sendMessage("${ChatColor.RED}An error has occurred!")
                        return true
                    }
                }
            }
            "quickjoin" -> {
                if (!sender.hasPermission(Permission.JOIN)) {
                    sender.sendMessage(ErrorMessages.NO_PERMISSION)
                    return true
                }
                if (sender !is Player) {
                    sender.sendMessage(ErrorMessages.MUST_BE_PLAYER)
                    return true
                }
                var maxGame: Pair<BedwarsGame, Int>? = null
                for (game in plugin.gameManager.getGames().values) {
                    if (game.state != GameState.LOBBY) {
                        continue
                    }
                    if (game.getPlayersInGame().size >= game.data.maxPlayers) {
                        continue
                    }
                    if (maxGame == null) {
                        maxGame = Pair(game, game.data.maxPlayers - game.getPlayersInGame().size)
                        continue
                    }
                    val other = maxGame.second
                    if (other > game.data.maxPlayers - game.getPlayersInGame().size) {
                        maxGame = Pair(game, game.data.maxPlayers - game.getPlayersInGame().size)
                    }
                }
                if (maxGame == null) {
                    sender.sendMessage("${ChatColor.RED}Could not find any games!")
                    return true
                }
                sender.sendMessage("${ChatColor.GREEN}Successfully found a game: ${maxGame.first.data.world.name}!")
                maxGame.first.addPlayer(sender)
                return true
            }
            "leave" -> {
                if (!sender.hasPermission(Permission.LEAVE)) {
                    sender.sendMessage(ErrorMessages.NO_PERMISSION)
                    return true
                }
                if (sender !is Player) {
                    sender.sendMessage(ErrorMessages.MUST_BE_PLAYER)
                    return true
                }
                val game = plugin.gameManager.getGame(sender)
                if (game == null) {
                    sender.sendMessage("${ChatColor.RED}You are not in a game!")
                    return true
                }
                game.leavePlayer(sender)
                sender.sendMessage("${ChatColor.GREEN}Successfully left the game!")
                return true
            }
            "reload" -> {
                if (!sender.hasPermission(Permission.RELOAD)) {
                    sender.sendMessage(ErrorMessages.NO_PERMISSION)
                    return true
                }
                val before = System.currentTimeMillis()
                sender.sendMessage("${ChatColor.YELLOW}Reloading...")
                plugin.reloadConfig()
                sender.sendMessage("${ChatColor.GREEN}Successfully reloaded in ${System.currentTimeMillis() - before}ms!")
                return true
            }
            "create" -> {
                if (!sender.hasPermission(Permission.SETUP)) {
                    sender.sendMessage(ErrorMessages.NO_PERMISSION)
                    return true
                }
                if (sender !is Player) {
                    sender.sendMessage(ErrorMessages.MUST_BE_PLAYER)
                    return true
                }
                val world = sender.world
                val worldName = world.name
                if (plugin.gameManager.getGame(worldName) != null) {
                    sender.sendMessage(ErrorMessages.GAME_ALREADY_EXISTS)
                    return true
                }
                if (plugin.gameManager.builders.keys.containsIgnoreCase(worldName)) {
                    sender.sendMessage(ErrorMessages.GAME_ALREADY_EXISTS)
                    return true
                }
                val builder = DataEditor(plugin, null, null, world).save()
                plugin.gameManager.builders[worldName] = builder
                sender.sendMessage("${ChatColor.GREEN}Successfully created a game!")
                return true
            }
            "delete" -> {
                if (!sender.hasPermission(Permission.SETUP)) {
                    sender.sendMessage(ErrorMessages.NO_PERMISSION)
                    return true
                }
                val game = hasPermissionAndValidGame(sender, args) ?: return true
                game.broadcast("${ChatColor.RED}Game force stopping!")
                game.forceStop()
                plugin.gameManager.deleteGame(game)
                sender.sendMessage("${ChatColor.GREEN}Successfully deleted the game!")
                return true
            }
            "save" -> {
                val game = hasPermissionAndValidGame(sender, args) ?: return true
                game.saveMap()
                plugin.dataFileManager.setEditing(game.data, false)
                sender.sendMessage("${ChatColor.GREEN}Successfully saved!")
                return true
            }
            "stop" -> {
                if (!sender.hasPermission(Permission.STOP)) {
                    sender.sendMessage(ErrorMessages.NO_PERMISSION)
                    return true
                }
                @Suppress("DuplicatedCode")
                if (args.size < 2) {
                    sender.sendMessage(ErrorMessages.TOO_LITTLE_ARGS)
                    return true
                }
                val game = plugin.gameManager.getGame(args[1])
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
            "start" -> {
                if (!sender.hasPermission(Permission.START)) {
                    sender.sendMessage(ErrorMessages.NO_PERMISSION)
                    return true
                }
                if (args.size < 2) {
                    sender.sendMessage(ErrorMessages.TOO_LITTLE_ARGS)
                    return true
                }
                @Suppress("DuplicatedCode")
                val game = plugin.gameManager.getGame(args[1])
                if (game == null) {
                    sender.sendMessage(ErrorMessages.INVALID_GAME)
                    return true
                }
                val result = game.start(true)
                if (result == BedwarsGame.Result.SUCCESS) {
                    sender.sendMessage("${ChatColor.GREEN}Successfully started the game!")
                    return true
                }
                sender.sendMessage("${ChatColor.RED}Error while starting the game: ${result.message}")
                return true
            }
            "edit" -> {
                val game = hasPermissionAndValidGame(sender, args, editing = true) ?: return true
                if (game.state != GameState.STOPPED) {
                    sender.sendMessage("${ChatColor.RED}The game is still running! do /$label stop ${game.data.world.name} to stop it!")
                    return true
                }
                if (plugin.dataFileManager.isEditing(game.data)) {
                    sender.sendMessage("${ChatColor.RED}The game is already in edit mode!")
                    return true
                }
                plugin.dataFileManager.setEditing(game.data, true)
                sender.sendMessage(
                    "${ChatColor.GREEN}Successfully set the game mode to edit! " +
                            "Do ${ChatColor.GOLD}/$label save ${game.data.world.name} ${ChatColor.GREEN}to save it, " +
                            "or ${ChatColor.GOLD}/$label setup ${game.data.world.name} finish ${ChatColor.GREEN}to setup finish (if this is first setup)!"
                )
                return true
            }
            "setup" -> {
                if (args.size < 3) {
                    sender.sendMessage(ErrorMessages.TOO_LITTLE_ARGS)
                    return true
                }
                val editor = getEditorFromWorld(args[1], sender) ?: return true
                if (sender !is Player) {
                    sender.sendMessage(ErrorMessages.MUST_BE_PLAYER)
                    return true
                }
                if (sender.world.name != editor.data.world.default(sender.world).name) {
                    sender.sendMessage("${ChatColor.RED}You must be in the same world as the arena!")
                    return true
                }
                when (args[2].toLowerCase()) {
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
                    "finish" -> {
                        val gameData = editor.data.build()
                        if (gameData == null) {
                            sender.sendMessage(
                                "${ChatColor.RED}Could not create the game! " +
                                        "Do ${ChatColor.GOLD}/$label setup ${editor.data.world.name} ready ${ChatColor.RED}for more information!"
                            )
                            return true
                        }
                        sender.sendMessage("${ChatColor.GREEN}Saving the data...")
                        gameData.save(plugin)
                        val game = BedwarsGame(plugin, gameData)
                        game.saveMap()
                        plugin.dataFileManager.setEditing(gameData, false)
                        plugin.gameManager.builders.remove(gameData.world.name)
                        plugin.gameManager.addGame(game)
                        sender.sendMessage("${ChatColor.GREEN}Successfully saved! (Map: ${game.data.world.name})")
                        return true
                    }
                    "lobby" -> {
                        editor.data.lobby = sender.location
                        editor.save()
                        sender.sendMessage("${ChatColor.GREEN}Successfully set the lobby!")
                        return true
                    }
                    "spec" -> {
                        editor.data.spec = sender.location
                        editor.save()
                        sender.sendMessage("${ChatColor.GREEN}Successfully set the spectator spawn!")
                        return true
                    }
                    "minplayers" -> {
                        if (args.size < 4) {
                            sender.sendMessage("${ChatColor.GOLD}Minimum Players: ${editor.data.minPlayers}")
                            return true
                        }
                        val min = args[3].toIntOrNull()
                        if (min == null) {
                            sender.sendMessage("${ChatColor.RED}'${ChatColor.GOLD}${args[3]}${ChatColor.RED}' isn't a valid number!")
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
                        if (args.size < 4) {
                            sender.sendMessage("${ChatColor.GOLD}Maximum Players: ${editor.data.maxPlayers}")
                            return true
                        }
                        val max = args[3].toIntOrNull()
                        if (max == null) {
                            sender.sendMessage("${ChatColor.RED}'${ChatColor.GOLD}${args[3]}${ChatColor.RED}' isn't a valid number!")
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
                        shopCommand(sender, editor, args, editor.data.shopVillagers, "shop")
                        return true
                    }
                    "upgrades" -> {
                        shopCommand(sender, editor, args, editor.data.upgradeVillagers, "upgrades")
                        return true
                    }
                    "spawner" -> {
                        if (args.size < 4) {
                            sender.sendMessage(ErrorMessages.TOO_LITTLE_ARGS)
                            return true
                        }
                        if (args[3].equals("add", ignoreCase = true)) {
                            if (args.size < 5) {
                                sender.sendMessage(ErrorMessages.TOO_LITTLE_ARGS)
                                return true
                            }
                            val type = SpawnerType.fromString(args[4])
                            if (type == null) {
                                sender.sendMessage(ErrorMessages.INVALID_ARG)
                                return true
                            }
                            editor.data.spawners.add(SpawnerData(type, sender.location))
                            sender.sendMessage("${ChatColor.GREEN}Successfully created a spawner!")
                            editor.save()
                            return true
                        }
                        if (!args[3].equals("remove", ignoreCase = true)) {
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
                        if (args.size < 4) {
                            sender.sendMessage("${ChatColor.GREEN}Teams:")
                            for (teamData in editor.data.teams) {
                                val team = teamData.team
                                val bed = editor.data.beds.firstOrNull { d -> d.team == team }
                                sender.sendMessage(
                                    "${team.displayName.formatWithColors(team.color)}${ChatColor.GOLD}: " +
                                            "Is bed set: ${(bed != null).getGreenOrRed()}" +
                                            if (bed != null) "Yes" else {
                                                "No"
                                            }
                                )
                            }
                            return true
                        }
                        if (args.size < 5) {
                            sender.sendMessage(ErrorMessages.TOO_LITTLE_ARGS)
                            return true
                        }
                        if (args[3].equals("add", ignoreCase = true)) {
                            val location = sender.location
                            val team = Team.fromString(args[4])
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
                                "${ChatColor.GREEN}Successfully created the team ${
                                    team.displayName.capAndFormat().formatWithColors(team.color)
                                }${ChatColor.GREEN}!"
                            )
                            return true
                        }
                        if (!args[3].equals("remove", ignoreCase = true)) {
                            sender.sendMessage(ErrorMessages.INVALID_ARG)
                            return true
                        }
                        val team = Team.fromString(args[4])
                        if (team == null) {
                            sender.sendMessage(ErrorMessages.INVALID_TEAM)
                            return true
                        }
                        if (!editor.data.teams.containsTeam(team)) {
                            sender.sendMessage("${ChatColor.RED}The team doesn't exist!")
                            return true
                        }
                        editor.data.teams.removeTeam(team)
                        editor.save()
                        sender.sendMessage(
                            "${ChatColor.GREEN}Successfully removed the team ${
                                team.displayName.capAndFormat().formatWithColors(team.color)
                            }${ChatColor.GREEN}!"
                        )
                        return true
                    }
                    "bed" -> {
                        if (args.size < 5) {
                            sender.sendMessage(ErrorMessages.TOO_LITTLE_ARGS)
                            return true
                        }
                        if (args[3].equals("add", ignoreCase = true)) {
                            val team = Team.fromString(args[4])
                            if (team == null) {
                                sender.sendMessage(ErrorMessages.INVALID_TEAM)
                                return true
                            }
                            val block = sender.location.block
                            if (!(block.type == Material.BED_BLOCK || block.type == Material.BED)) {
                                sender.sendMessage("${ChatColor.RED}You are not standing on a bed!")
                                return true
                            }
                            for (bed in editor.data.beds) {
                                if (bed.team == team) {
                                    sender.sendMessage("${ChatColor.RED}A bed for that team already exists!")
                                    return true
                                }
                                if (bed.location.getWrapper() == block.getBedHead().getWrapper()) {
                                    sender.sendMessage("${ChatColor.RED}A bed for that team already exists!")
                                    return true
                                }
                            }
                            editor.data.beds.add(BedData.getBedData(team, block))
                            sender.sendMessage("${ChatColor.GREEN}Successfully set the bed!")
                            editor.save()
                            return true
                        }
                        @Suppress("DuplicatedCode")
                        if (!args[3].equals("remove", ignoreCase = true)) {
                            sender.sendMessage(ErrorMessages.INVALID_ARG)
                            return true
                        }
                        val team = Team.fromString(args[4])
                        if (team == null) {
                            sender.sendMessage(ErrorMessages.INVALID_TEAM)
                            return true
                        }
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
        args: Array<out String>,
        villagers: MutableSet<UUID>,
        type: String
    ) {
        if (args.size < 4) {
            sender.sendMessage(ErrorMessages.TOO_LITTLE_ARGS)
            return
        }
        val entity = sender.getLookingAt()
        val allVillagers = editor.data.shopVillagers.plus(editor.data.upgradeVillagers)
        if (entity == null || entity.type != EntityType.VILLAGER) {
            sender.sendMessage("${ChatColor.RED}You are not looking at a villager close enough!")
            return
        }
        if (args[3].equals("add", ignoreCase = true)) {
            if (allVillagers.contains(entity.uniqueId)) {
                sender.sendMessage("${ChatColor.RED}That entity is already a shop/upgrades villager!")
                return
            }
            villagers.add(entity.uniqueId)
            editor.save()
            for (villager in villagers) {
                val e = villager.getEntity() ?: continue
                e.removeAI()
            }
            sender.sendMessage("${ChatColor.GREEN}Successfully added the $type villager!")
            editor.save()
            return
        }
        if (!args[3].equals("remove", ignoreCase = true)) {
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

    private fun hasPermissionAndValidGame(
        sender: CommandSender,
        args: Array<out String>,
        showMsgForGame: Boolean = true,
        editing: Boolean = false
    ): BedwarsGame? {
        if (!sender.hasPermission(Permission.SETUP)) {
            sender.sendMessage(ErrorMessages.NO_PERMISSION)
            return null
        }
        if (args.size < 2) {
            sender.sendMessage(ErrorMessages.TOO_LITTLE_ARGS)
            return null
        }
        val game = plugin.gameManager.getGame(args[1])
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

    private fun getEditorFromWorld(worldName: String, sender: CommandSender): DataEditor? {
        val world = Bukkit.getWorld(worldName)
        if (world == null) {
            sender.sendMessage(ErrorMessages.INVALID_WORLD)
            return null
        }
        val editor = DataEditor.findFromWorld(world, plugin)
        if (editor == null) {
            sender.sendMessage(ErrorMessages.INVALID_GAME)
            return null
        }
        return editor
    }
}