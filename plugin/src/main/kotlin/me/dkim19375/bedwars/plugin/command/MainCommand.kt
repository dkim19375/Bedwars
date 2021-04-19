package me.dkim19375.bedwars.plugin.command

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.enumclass.GameState
import me.dkim19375.bedwars.plugin.enumclass.Permission
import me.dkim19375.bedwars.plugin.manager.BedwarsGame
import me.dkim19375.bedwars.plugin.util.hasPermission
import me.dkim19375.bedwars.plugin.util.showHelpMessage
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

private const val TOO_LITTLE_ARGS = "Not enough arguments!"
private const val INVALID_GAME = "That is not a valid game!"
private const val INVALID_ARG = "Invalid argument!"
private const val MUST_BE_PLAYER = "You must be a player!"

class MainCommand(private val plugin: BedwarsPlugin) : CommandExecutor {
    @Suppress("PrivatePropertyName")
    private val NO_PERMISSION =
        Bukkit.getPluginCommand("bedwars")?.permissionMessage ?: "You do not have permission to run this command!"

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (args.isEmpty()) {
            sender.showHelpMessage(label, TOO_LITTLE_ARGS)
            return true
        }
        @Suppress("LiftReturnOrAssignment")
        when (args[0].toLowerCase()) {
            "help" -> {
                if (!sender.hasPermission(Permission.HELP)) {
                    sender.sendMessage("${ChatColor.RED}$NO_PERMISSION")
                    return true
                }
                if (args.size > 1) {
                    sender.showHelpMessage(
                        label,
                        null,
                        if (args[1].toIntOrNull() == null) 1 else args[1].toIntOrNull()!!
                    )
                    return true
                }
                sender.showHelpMessage(label, null)
                return true
            }
            "list" -> {
                if (!sender.hasPermission(Permission.LIST)) {
                    sender.showHelpMessage(label, NO_PERMISSION)
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
                    sender.showHelpMessage(label, NO_PERMISSION)
                    return true
                }
                if (sender !is Player) {
                    sender.showHelpMessage(label, MUST_BE_PLAYER)
                    return true
                }
                if (args.size < 2) {
                    sender.showHelpMessage(label, TOO_LITTLE_ARGS)
                    return true
                }
                val game = plugin.gameManager.getGame(args[1])
                if (game == null) {
                    sender.showHelpMessage(label, INVALID_GAME)
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
                    sender.showHelpMessage(label, NO_PERMISSION)
                    return true
                }
                if (sender !is Player) {
                    sender.showHelpMessage(label, MUST_BE_PLAYER)
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
                sender.sendMessage("${ChatColor.GREEN}Successfully found a game: ${maxGame.first.data.displayName}!")
                maxGame.first.addPlayer(sender)
                return true
            }
            "leave" -> {
                if (!sender.hasPermission(Permission.LEAVE)) {
                    sender.showHelpMessage(label, NO_PERMISSION)
                    return true
                }
                if (sender !is Player) {
                    sender.showHelpMessage(label, MUST_BE_PLAYER)
                    return true
                }
                val game = plugin.gameManager.getGame(sender)
                if (game == null) {
                    sender.showHelpMessage(label, INVALID_GAME)
                    return true
                }
                game.leavePlayer(sender)
                sender.sendMessage("${ChatColor.GREEN}Successfully left the game!")
                return true
            }
            "reload" -> {
                if (!sender.hasPermission(Permission.RELOAD)) {
                    sender.showHelpMessage(label, NO_PERMISSION)
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
                    sender.showHelpMessage(label, NO_PERMISSION)
                    return true
                }
                TODO()
            }
            "delete" -> {
                if (!sender.hasPermission(Permission.SETUP)) {
                    sender.showHelpMessage(label, NO_PERMISSION)
                    return true
                }
                val game = hasPermissionAndValidGame(sender, label, args) ?: return true
                game.broadcast("${ChatColor.RED}Game force stopping!")
                game.forceStop()
                plugin.gameManager.deleteGame(game)
                sender.sendMessage("${ChatColor.GREEN}Successfully deleted the game!")
                return true
            }
            "save" -> {
                val game = hasPermissionAndValidGame(sender, label, args) ?: return true
                plugin.dataFileManager.setEditing(game.data, false)
                return true
            }
            "stop" -> {
                if (!sender.hasPermission(Permission.STOP)) {
                    sender.showHelpMessage(label, NO_PERMISSION)
                    return true
                }
                val game = plugin.gameManager.getGame(args[1])
                if (game == null) {
                    sender.showHelpMessage(label, INVALID_GAME)
                    return true
                }
                game.broadcast("${ChatColor.RED}An admin has force stopped the game!")
                game.forceStop()
                sender.sendMessage("${ChatColor.GREEN}Successfully stopped the game!")
                return true
            }
            "edit" -> {
                val game = hasPermissionAndValidGame(sender, label, args) ?: return true
                if (game.state != GameState.STOPPED) {
                    sender.sendMessage("${ChatColor.RED}The game is still running! do /$label stop ${game.data.world.name} to stop it!")
                    return true
                }
                if (plugin.dataFileManager.isEditing(game.data)) {
                    sender.sendMessage("${ChatColor.RED}The game is already in edit mode!")
                    return true
                }
                plugin.dataFileManager.setEditing(game.data, true)
                sender.sendMessage("${ChatColor.GREEN}Successfully set the game mode to edit! Do /$label save ${game.data.world.name} to save it!")
                return true
            }
            "setup" -> {
                val game = hasPermissionAndValidGame(sender, label, args) ?: return true
                return true
            }
            else -> {
                sender.showHelpMessage(label, INVALID_ARG)
                return true
            }
        }
    }

    private fun hasPermissionAndValidGame(sender: CommandSender, label: String, args: Array<out String>): BedwarsGame? {
        if (!sender.hasPermission(Permission.SETUP)) {
            sender.showHelpMessage(label, NO_PERMISSION)
            return null
        }
        if (args.size < 2) {
            sender.showHelpMessage(label, TOO_LITTLE_ARGS)
            return null
        }
        val game = plugin.gameManager.getGame(args[1])
        if (game == null) {
            sender.showHelpMessage(label, INVALID_GAME)
        }
        return game
    }
}