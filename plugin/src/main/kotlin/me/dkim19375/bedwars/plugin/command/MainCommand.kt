package me.dkim19375.bedwars.plugin.command

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.builder.DataEditor
import me.dkim19375.bedwars.plugin.data.BedData
import me.dkim19375.bedwars.plugin.data.SpawnerData
import me.dkim19375.bedwars.plugin.data.TeamData
import me.dkim19375.bedwars.plugin.enumclass.*
import me.dkim19375.bedwars.plugin.manager.BedwarsGame
import me.dkim19375.bedwars.plugin.util.*
import me.dkim19375.dkim19375core.function.filterNonNull
import me.tigerhix.lib.scoreboard.type.Entry
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
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
            sender.showHelpMsg(label)
            return true
        }
        @Suppress("LiftReturnOrAssignment")
        when (args[0].toLowerCase()) {
            "heal-pool" -> {
                if (!check(sender, command, label, args, 1, Permission.SETUP, true)) {
                    return true
                }
                val player = sender as Player
                val game = plugin.gameManager.getGame(player)
                if (game == null) {
                    sender.sendMessage(ErrorMessages.INVALID_GAME)
                    return true
                }
                val team = game.getTeamOfPlayer(player)
                if (team == null) {
                    sender.sendMessage(ErrorMessages.INVALID_TEAM)
                    return true
                }
                if (!game.upgradesManager.healPool.contains(team)) {
                    sender.sendMessage("Doesn't have heal pool")
                    return true
                }
                var has = false
                for (d in game.data.beds) {
                    sender.sendMessage("Team: ${d.team.name}, is same: ${team == d.team}, " +
                            "distance: ${d.location.getSafeDistance(player.location) < 7}. " +
                            "Overall: ${team == d.team && d.location.getSafeDistance(player.location) < 7}")
                    if (team == d.team && d.location.getSafeDistance(player.location) < 7) {
                        has = true
                    }
                }
                sender.sendMessage(" ")
                sender.sendMessage("${ChatColor.AQUA}Overall: $has")
                return true
            }
            "inv" -> {
                if (!check(sender, command, label, args, 1, Permission.SETUP, true)) {
                    return true
                }
                val player = sender as Player
                val inv = player.inventory
                player.sendMessage("contents: ${inv.contents.toList().filterNonNull()}")
                player.sendMessage("armorContents: ${inv.armorContents.toList().filterNonNull()}")
                player.sendMessage("all: ${inv.getAllContents().toList().filterNonNull()}")
                return true
            }
            "listname" -> {
                if (!check(sender, command, label, args, 1, Permission.SETUP, true)) {
                    return true
                }
                val player = sender as Player
                val game = plugin.gameManager.getGame(player)
                if (game == null) {
                    sender.showHelpMsg(label, ErrorMessages.INVALID_GAME)
                    return true
                }
                val team = game.getTeamOfPlayer(player)
                if (team == null) {
                    sender.showHelpMsg(label, "${ChatColor.RED}You must be in a team!")
                    return true
                }
                if (args.size < 2) {
                    player.sendMessage("List name: ${player.playerListName}")
                    return true
                }
                player.playerListName = if (args[1] == "null") null else args[1]
                player.sendMessage("List name set to: ${team.chatColor}${if (args[1] == "null") null else args[1]}")
                return true
            }
            "item" -> {
                if (!check(sender, command, label, args, 2, Permission.SETUP, true)) {
                    return true
                }
                val player = sender as Player
                val item = try {
                    MainShopItems.valueOf(args[1].toUpperCase())
                } catch (_: IllegalArgumentException) {
                    player.sendMessage(ErrorMessages.INVALID_ARG)
                    return true
                }
                player.inventory.addItem(item.item.toItemStack(plugin.gameManager.getTeamOfPlayer(player)?.color))
                player.sendMessage("${ChatColor.GREEN}Successfully gave item!")
                return true
            }
            "board" -> {
                if (!check(sender, command, label, args, 1, Permission.SETUP, true)) {
                    return true
                }
                val player = sender as Player
                val entries = plugin.scoreboardManager.getEntries(player)
                if (entries.isNullOrEmpty()) {
                    player.sendMessage("${ChatColor.RED}Empty scoreboard!")
                    return true
                }
                val list = entries.map(Entry::getName)
                player.sendMessage("${ChatColor.DARK_BLUE}------------------------------------------------")
                player.sendMessage(plugin.scoreboardManager.getTitle(player))
                player.sendMessage("${ChatColor.DARK_GRAY}------------------------------------------------")
                list.forEach(player::sendMessage)
                player.sendMessage("${ChatColor.DARK_BLUE}------------------------------------------------")
                return true
            }
            "sound" -> {
                if (!check(sender, command, label, args, 3, Permission.SETUP, true)) {
                    return true
                }
                val player = sender as Player
                val sound: Sound = try {
                    Sound.valueOf(args[1].toUpperCase())
                } catch (_: IllegalArgumentException) {
                    player.sendMessage(ErrorMessages.INVALID_ARG)
                    return true
                }
                val pitch: Float? = args[2].toFloatOrNull()
                if (pitch == null) {
                    player.sendMessage(ErrorMessages.INVALID_ARG)
                    return true
                }
                player.playSound(sound, pitch = pitch)
                player.sendMessage("${ChatColor.GREEN}Played sound: ${sound.name} at pitch $pitch!")
                return true
            }
            "debug" -> {
                if (!check(sender, command, label, args, 1, Permission.SETUP, true)) {
                    return true
                }
                val player = sender as Player
                val game: BedwarsGame
                if (args.size > 1) {
                    val g = plugin.gameManager.getGame(args[1])
                    if (g == null) {
                        player.sendMessage(ErrorMessages.INVALID_GAME)
                        return true
                    }
                    game = g
                } else {
                    val g = plugin.gameManager.getGame(player)
                    if (g == null) {
                        player.sendMessage(ErrorMessages.INVALID_GAME)
                        return true
                    }
                    game = g
                }
                player.sendMessage("Test: ${plugin.gameManager.getGame(player) != null}")
                player.sendMessage(
                    "Is in lobby OR game: ${
                        if (game.playersInLobby.plus(game.players.values.getCombinedValues())
                                .contains(player.uniqueId)
                        ) "Yes" else "No"
                    }"
                )
                player.sendMessage("Game name: ${game.data.world.name}")
                player.sendMessage("Players: ${game.getPlayersInGame().size}")
                player.sendMessage("State: ${game.state.name}")
                player.sendMessage(
                    "Player list: ${
                        game.getPlayersInGame().getPlayers().getUsernames().joinToString(", ")
                    }"
                )
                player.sendMessage("Is in lobby: ${if (game.playersInLobby.contains(player.uniqueId)) "Yes" else "No"}")
                player.sendMessage(
                    "Is in game: ${
                        if (game.players.values.getCombinedValues().contains(player.uniqueId)) "Yes" else "No"
                    }"
                )
                player.sendMessage("Can start: ${game.canStart(false).name}, message: ${game.canStart(false).message}")
                player.sendMessage("Is spawner runnable not-null: ${game.spawnerManager.runnable != null}")
                return true
            }
            "help" -> {
                if (!sender.hasPermission(Permission.HELP)) {
                    sender.sendMessage(ErrorMessages.NO_PERMISSION)
                    return true
                }
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
                if (!check(sender, command, label, args, 2, Permission.JOIN, true)) {
                    return true
                }
                val player = sender as Player
                val game = plugin.gameManager.getGame(args[1])
                if (game == null) {
                    player.sendMessage(ErrorMessages.INVALID_GAME)
                    return true
                }
                when (game.addPlayer(player)) {
                    BedwarsGame.Result.SUCCESS -> {
                        player.sendMessage("${ChatColor.GREEN}Successfully joined the bedwars game!")
                        return true
                    }
                    BedwarsGame.Result.GAME_RUNNING -> {
                        player.sendMessage("${ChatColor.RED}The game is already running!")
                        return true
                    }
                    BedwarsGame.Result.TOO_MANY_PLAYERS -> {
                        player.sendMessage("${ChatColor.RED}The game is full!")
                        return true
                    }
                    else -> {
                        player.sendMessage("${ChatColor.RED}An error has occurred!")
                        return true
                    }
                }
            }
            "quickjoin" -> {
                if (!check(sender, command, label, args, 1, Permission.JOIN, true)) {
                    return true
                }
                val player = sender as Player
                var maxGame: Pair<BedwarsGame, Int>? = null
                for (game in plugin.gameManager.getGames().values) {
                    if (game.state != GameState.LOBBY && game.state != GameState.STARTING) {
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
                    player.sendMessage("${ChatColor.RED}Could not find any games!")
                    return true
                }
                player.sendMessage("${ChatColor.GREEN}Successfully found a game: ${maxGame.first.data.world.name}!")
                maxGame.first.addPlayer(player)
                return true
            }
            "leave" -> {
                if (!check(sender, command, label, args, 1, Permission.LEAVE, true)) {
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
                if (!check(sender, command, label, args, 2, Permission.SETUP, true)) {
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
                val game = hasPermissionAndValidGame(sender, command, label, args) ?: return true
                game.broadcast("${ChatColor.RED}Game force stopping!")
                game.forceStop()
                plugin.gameManager.deleteGame(game)
                sender.sendMessage("${ChatColor.GREEN}Successfully deleted the game!")
                return true
            }
            "save" -> {
                val game = hasPermissionAndValidGame(sender, command, label, args) ?: return true
                game.saveMap()
                plugin.dataFileManager.setEditing(game.data, false)
                game.state = GameState.LOBBY
                sender.sendMessage("${ChatColor.GREEN}Successfully saved!")
                return true
            }
            "stop" -> {
                if (!check(sender, command, label, args, 2, Permission.STOP, false)) {
                    return true
                }
                if (args[1].equals("all", ignoreCase = true)) {
                    val games = plugin.gameManager.getRunningGames().values
                    val amount = games.size
                    val start = System.currentTimeMillis()
                    for ((count, game) in games.toList().withIndex()) {
                        game.broadcast("${ChatColor.RED}An admin has force stopped the game!")
                        sender.sendMessage("${ChatColor.GREEN}Stopping the game..")
                        val time = System.currentTimeMillis()
                        game.forceStop {
                            sender.sendMessage("${ChatColor.GREEN}Successfully stopped game ${game.data.world.name} " +
                                    "(${count + 1}/$amount) (${System.currentTimeMillis() - time}ms)!")
                        }
                    }
                    sender.sendMessage("${ChatColor.GREEN}Successfully stopped $amount games in ${System.currentTimeMillis() - start}ms!")
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
                if (!check(sender, command, label, args, 2, Permission.START, false)) {
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
                val game = hasPermissionAndValidGame(sender, command, label, args, editing = true) ?: return true
                if (game.state != GameState.LOBBY) {
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
                if (sender !is Player) {
                    return true
                }
                sender.teleport(game.data.lobby)
                return true
            }
            "setup" -> {
                if (!check(sender, command, label, args, 3, Permission.SETUP, true)) {
                    return true
                }
                val player = sender as Player
                val editor = getEditorFromWorld(args[1], player) ?: return true
                if (player.world.name != editor.data.world.default(player.world).name) {
                    player.sendMessage("${ChatColor.RED}You must be in the same world as the arena!")
                    return true
                }
                when (args[2].toLowerCase()) {
                    "ready" -> {
                        val buildStatus = editor.data.canBuild()
                        if (buildStatus.isEmpty()) {
                            player.sendMessage("${ChatColor.GREEN}The game is ready to be saved!")
                            return true
                        }
                        player.sendMessage("${ChatColor.RED}There are ${buildStatus.size} errors!")
                        for (error in buildStatus) {
                            player.sendMessage("${ChatColor.GOLD}- ${error.message}")
                        }
                        return true
                    }
                    "finish" -> {
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
                        val game = BedwarsGame(plugin, gameData)
                        game.saveMap()
                        plugin.dataFileManager.setEditing(gameData, false)
                        plugin.gameManager.builders.remove(gameData.world.name)
                        plugin.gameManager.addGame(game)
                        player.sendMessage("${ChatColor.GREEN}Successfully saved! (Map: ${game.data.world.name})")
                        return true
                    }
                    "lobby" -> {
                        editor.data.lobby = player.location
                        editor.save()
                        player.sendMessage("${ChatColor.GREEN}Successfully set the lobby!")
                        return true
                    }
                    "spec" -> {
                        editor.data.spec = player.location
                        editor.save()
                        player.sendMessage("${ChatColor.GREEN}Successfully set the spectator spawn!")
                        return true
                    }
                    "minplayers" -> {
                        if (args.size < 4) {
                            player.sendMessage("${ChatColor.GOLD}Minimum Players: ${editor.data.minPlayers}")
                            return true
                        }
                        val min = args[3].toIntOrNull()
                        if (min == null) {
                            player.sendMessage("${ChatColor.RED}'${ChatColor.GOLD}${args[3]}${ChatColor.RED}' isn't a valid number!")
                            return true
                        }
                        if (min > editor.data.maxPlayers) {
                            player.sendMessage(
                                "${ChatColor.RED}The minimum (${ChatColor.GOLD}$min${ChatColor.RED}) " +
                                        "cannot be more than the maximum (${ChatColor.GOLD}${editor.data.maxPlayers}${ChatColor.RED}!"
                            )
                            return true
                        }
                        editor.data.minPlayers = min
                        editor.save()
                        player.sendMessage("${ChatColor.GREEN}Successfully set the minimum players to ${ChatColor.GOLD}$min${ChatColor.GREEN}!")
                        return true
                    }
                    "maxplayers" -> {
                        if (args.size < 4) {
                            player.sendMessage("${ChatColor.GOLD}Maximum Players: ${editor.data.maxPlayers}")
                            return true
                        }
                        val max = args[3].toIntOrNull()
                        if (max == null) {
                            player.sendMessage("${ChatColor.RED}'${ChatColor.GOLD}${args[3]}${ChatColor.RED}' isn't a valid number!")
                            return true
                        }
                        if (max < editor.data.minPlayers) {
                            player.sendMessage(
                                "${ChatColor.RED}The maximum (${ChatColor.GOLD}$max${ChatColor.RED}) " +
                                        "cannot be less than the minimum (${ChatColor.GOLD}${editor.data.minPlayers}${ChatColor.RED}!"
                            )
                            return true
                        }
                        editor.data.maxPlayers = max
                        editor.save()
                        player.sendMessage("${ChatColor.GREEN}Successfully set the maximum players to ${ChatColor.GOLD}$max${ChatColor.GREEN}!")
                        return true
                    }
                    "shop" -> {
                        shopCommand(player, editor, args, editor.data.shopVillagers, "shop")
                        return true
                    }
                    "upgrades" -> {
                        shopCommand(player, editor, args, editor.data.upgradeVillagers, "upgrades")
                        return true
                    }
                    "spawner" -> {
                        if (args.size < 4) {
                            player.sendMessage(ErrorMessages.TOO_LITTLE_ARGS)
                            return true
                        }
                        if (args[3].equals("add", ignoreCase = true)) {
                            if (args.size < 5) {
                                player.sendMessage(ErrorMessages.TOO_LITTLE_ARGS)
                                return true
                            }
                            val type = SpawnerType.fromString(args[4])
                            if (type == null) {
                                player.sendMessage(ErrorMessages.INVALID_ARG)
                                return true
                            }
                            editor.data.spawners.add(SpawnerData(type, player.location))
                            player.sendMessage("${ChatColor.GREEN}Successfully created a spawner!")
                            editor.save()
                            return true
                        }
                        if (!args[3].equals("remove", ignoreCase = true)) {
                            player.sendMessage(ErrorMessages.INVALID_ARG)
                            return true
                        }
                        var closestSpawner: Pair<SpawnerData, Double>? = null
                        for (spawner in editor.data.spawners) {
                            val distance = spawner.location.getSafeDistance(player.location)
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
                            player.sendMessage("${ChatColor.RED}There are no nearby spawners!")
                            return true
                        }
                        val spawner = closestSpawner.first
                        editor.data.spawners.remove(spawner)
                        player.sendMessage("${ChatColor.GREEN}Successfully removed a ${spawner.type.name.capAndFormat()} spawner!")
                        editor.save()
                        return true
                    }
                    "team" -> {
                        if (args.size < 4) {
                            player.sendMessage("${ChatColor.GREEN}Teams:")
                            for (teamData in editor.data.teams) {
                                val team = teamData.team
                                val bed = editor.data.beds.firstOrNull { d -> d.team == team }
                                player.sendMessage(
                                    "${team.chatColor}${team.displayName}${ChatColor.GOLD}: " +
                                            "Is bed set: ${(bed != null).getGreenOrRed()}" +
                                            if (bed != null) "Yes" else {
                                                "No"
                                            }
                                )
                            }
                            return true
                        }
                        if (args.size < 5) {
                            player.sendMessage(ErrorMessages.TOO_LITTLE_ARGS)
                            return true
                        }
                        if (args[3].equals("add", ignoreCase = true)) {
                            val location = player.location
                            val team = Team.fromString(args[4])
                            if (team == null) {
                                player.sendMessage(ErrorMessages.INVALID_TEAM)
                                return true
                            }
                            if (editor.data.teams.containsTeam(team)) {
                                player.sendMessage("${ChatColor.RED}The team already exists!")
                                return true
                            }
                            editor.data.teams.setData(TeamData(team, location))
                            editor.save()
                            player.sendMessage(
                                "${ChatColor.GREEN}Successfully created the team ${team.chatColor}${team.displayName.capAndFormat()}${ChatColor.GREEN}!"
                            )
                            return true
                        }
                        if (!args[3].equals("remove", ignoreCase = true)) {
                            player.sendMessage(ErrorMessages.INVALID_ARG)
                            return true
                        }
                        val team = Team.fromString(args[4])
                        if (team == null) {
                            player.sendMessage(ErrorMessages.INVALID_TEAM)
                            return true
                        }
                        if (!editor.data.teams.containsTeam(team)) {
                            player.sendMessage("${ChatColor.RED}The team doesn't exist!")
                            return true
                        }
                        editor.data.teams.removeTeam(team)
                        editor.save()
                        player.sendMessage(
                            "${ChatColor.GREEN}Successfully removed the team ${team.chatColor}${
                                team.displayName.capAndFormat()
                            }${ChatColor.GREEN}!"
                        )
                        return true
                    }
                    "bed" -> {
                        if (args.size < 5) {
                            player.sendMessage(ErrorMessages.TOO_LITTLE_ARGS)
                            return true
                        }
                        if (args[3].equals("add", ignoreCase = true)) {
                            val team = Team.fromString(args[4])
                            if (team == null) {
                                player.sendMessage(ErrorMessages.INVALID_TEAM)
                                return true
                            }
                            val block = player.location.block
                            if (!(block.type == Material.BED_BLOCK || block.type == Material.BED)) {
                                player.sendMessage("${ChatColor.RED}You are not standing on a bed!")
                                return true
                            }
                            for (bed in editor.data.beds) {
                                if (bed.team == team) {
                                    player.sendMessage("${ChatColor.RED}A bed for that team already exists!")
                                    return true
                                }
                                if (bed.location.getWrapper() == block.getBedHead().getWrapper()) {
                                    player.sendMessage("${ChatColor.RED}A bed for that team already exists!")
                                    return true
                                }
                            }
                            editor.data.beds.add(BedData.getBedData(team, block))
                            player.sendMessage("${ChatColor.GREEN}Successfully set the bed!")
                            editor.save()
                            return true
                        }
                        @Suppress("DuplicatedCode")
                        if (!args[3].equals("remove", ignoreCase = true)) {
                            player.sendMessage(ErrorMessages.INVALID_ARG)
                            return true
                        }
                        val team = Team.fromString(args[4])
                        if (team == null) {
                            player.sendMessage(ErrorMessages.INVALID_TEAM)
                            return true
                        }
                        if (editor.data.beds.none { data -> data.team == team }) {
                            player.sendMessage("${ChatColor.RED}The team doesn't have a bed setup!")
                            return true
                        }
                        editor.data.beds.removeIf { data -> data.team == team }
                        editor.save()
                        player.sendMessage("${ChatColor.GREEN}Successfully removed the bed!")
                        return true
                    }
                    else -> {
                        player.sendMessage(ErrorMessages.INVALID_ARG)
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
            entity.teleport(sender.location.getOppositeYaw())
            editor.save()
            for (villager in villagers) {
                val e = villager.getEntity() ?: continue
                e.removeAI()
            }
            plugin.gameManager.getGames().values.forEach { g -> g.npcManager.disableAI() }
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

    @Suppress("unused_parameter")
    fun check(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
        minArgs: Int,
        permission: Permission,
        bePlayer: Boolean): Boolean {
        if (!sender.hasPermission(permission)) {
            sender.showHelpMsg(label, ErrorMessages.NO_PERMISSION)
            return false
        }
        if (sender !is Player && bePlayer) {
            sender.showHelpMsg(label, ErrorMessages.MUST_BE_PLAYER)
            return false
        }
        if (minArgs > args.size) {
            sender.showHelpMsg(label, ErrorMessages.TOO_LITTLE_ARGS)
            return false
        }
        return true
    }

    private fun hasPermissionAndValidGame(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
        showMsgForGame: Boolean = true,
        editing: Boolean = false
    ): BedwarsGame? {
        if (!check(sender, command, label, args, 2, Permission.SETUP, !editing)) {
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