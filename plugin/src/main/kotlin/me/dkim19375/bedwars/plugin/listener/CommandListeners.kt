/*
 * MIT License
 *
 * Copyright (c) 2021 dkim19375
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.enumclass.GameState
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
        if (plugin.gameManager.getGames().values.none { g -> g.state != GameState.LOBBY }) {
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