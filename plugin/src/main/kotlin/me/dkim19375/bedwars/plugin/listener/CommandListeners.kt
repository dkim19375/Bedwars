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

package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.api.enumclass.GameState
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
        if (args[0].equals("reload", ignoreCase = true)) {
            event.isCancelled = true
            sender.sendMessage("${ChatColor.RED}Reloads disabled due to known bugs with reloading!")
            return
        }
        if (!args[0].equals("stop", ignoreCase = true)) {
            return
        }
        if (plugin.gameManager.getGames().values.filter { !it.isEditing() }.none { game ->
                game.state != GameState.LOBBY
            }) {
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