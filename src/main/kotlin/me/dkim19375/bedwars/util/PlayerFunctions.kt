package me.dkim19375.bedwars.util

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

fun CommandSender.showHelpMessage(label: String, error: String?) {
    sendMessage("${ChatColor.DARK_BLUE}------------------------------------------------")
    sendMessage("${ChatColor.GREEN}Bedwars Help Page")
    sendHelpMsgFormatted(label, "help", "Show this help menu")
    sendHelpMsgFormatted(label, "list", "List bedwars maps and games")
    sendHelpMsgFormatted(label, "create <name>", "Create a new bedwars game")
    sendHelpMsgFormatted(label, "delete <name>", "Delete a bedwars game")
    error?.let {
        sendMessage("${ChatColor.RED}$it")
    }
    sendMessage("${ChatColor.DARK_BLUE}------------------------------------------------")
}

private fun CommandSender.sendHelpMsgFormatted(label: String, arg: String, description: String) {
    sendMessage("${ChatColor.AQUA}/$label $arg - ${ChatColor.GOLD}$description")
}

fun List<UUID>.getPlayers() = map(Bukkit::getPlayer).filter(Objects::nonNull)

fun Player.getItemAmount(type: Material): Int {
    var amount = 0
    val inv = inventory.contents.toList()
    inv.forEach { item ->
        if (item.type == type) {
            amount++
        }
    }
    return amount
}