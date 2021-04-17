package me.dkim19375.bedwars.plugin.util

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.kyori.adventure.util.Ticks
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
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

fun Set<UUID>.getPlayers() = map(Bukkit::getPlayer).filter(Objects::nonNull)

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

fun Player.playSound(sound: Sound, volume: Float = 1.0f, pitch: Float = 1.0f) {
    playSound(location, sound, volume, pitch)
}

fun Player.sendTitle(title: String? = null, subtitle: String? = null, fadeIn: Int = 10, stay: Int = 50, fadeOut: Int = 10) {
    val newTitle = Component.text(title?: "")
    val newSubTitle = Component.text(subtitle?: "")
    val times =
        Title.Times.of(Ticks.duration(fadeIn.toLong()), Ticks.duration(stay.toLong()), Ticks.duration(fadeOut.toLong()))
    val cTitle = Title.title(newTitle, newSubTitle, times)
    val audience = BukkitAudiences.create(JavaPlugin.getPlugin(BedwarsPlugin::class.java)).player(this)
    audience.showTitle(cTitle)
}