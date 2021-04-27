package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.server.ServerCommandEvent

class CommandListeners(private val plugin: BedwarsPlugin) : Listener {
    private val commandRunningMsg = listOf(
        "${ChatColor.RED}A game is currently running! Please stop them, or else many problems WILL OCCUR!",
        "${ChatColor.RED}Do /bedwars stop all to stop all the games!",
        "${ChatColor.RED}If you still want to stop the server, do /stop force (NOT RECOMMENDED!)"
    )

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private fun ServerCommandEvent.onCommandSend() {
        onCommandEvent(command, sender, this)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private fun PlayerCommandPreprocessEvent.onSend() {
        onCommandEvent(message.removePrefix("/"), player, this)
    }

    private fun onCommandEvent(command: String, sender: CommandSender, event: Cancellable) {
        val args = command.trim().split(" ")
        if (args.isEmpty()) {
            return
        }
        if (!args[0].equals("stop", ignoreCase = true)) {
            return
        }
        if (plugin.gameManager.getRunningGames().isEmpty()) {
            return
        }
        if (args.size < 2) {
            commandRunningMsg.forEach(sender::sendMessage)
            event.isCancelled = true
            return
        }
        if (args[1].equals("force", ignoreCase = true)) {
            return
        }
        commandRunningMsg.forEach(sender::sendMessage)
        event.isCancelled = true
    }
}