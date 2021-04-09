package me.dkim19375.bedwars.plugin.command

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.util.showHelpMessage
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

private const val TOO_LITTLE_ARGS = "Not enough arguments!"
private const val TOO_MANY_ARGS = "Too many arguments!"
private const val INVALID_GAME = "That is not a valid game!"
private const val INVALID_ARG = "Invalid argument!"

class MainCommand(private val plugin: BedwarsPlugin) : CommandExecutor {
    @Suppress("PrivatePropertyName")
    private val NO_PERMISSION = Bukkit.getPluginCommand("bedwars")!!.permissionMessage

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
                sender.showHelpMessage(label, null)
                return true
            }
            else -> {
                sender.showHelpMessage(label, INVALID_ARG)
                return true
            }
        }
    }
}