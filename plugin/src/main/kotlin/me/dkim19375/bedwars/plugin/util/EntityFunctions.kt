package me.dkim19375.bedwars.plugin.util

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.data.HelpMessage
import me.dkim19375.bedwars.plugin.enumclass.ErrorMessages
import me.dkim19375.bedwars.plugin.enumclass.Permission
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.title.Title
import net.kyori.adventure.util.Ticks
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.permissions.Permissible
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import kotlin.math.ceil

val commands = listOf(
    HelpMessage("help [page]", "Show this help menu", Permission.HELP),
    HelpMessage("list", "List bedwars maps and games", Permission.LIST),
    HelpMessage("join <name>", "Join a bedwars game", Permission.JOIN),
    HelpMessage("quickjoin", "Quickly a bedwars game", Permission.JOIN),
    HelpMessage("leave", "Leave a bedwars game", Permission.LEAVE),
    HelpMessage("reload", "Reload the plugin's config files", Permission.RELOAD),
    HelpMessage("create", "Create a new bedwars game", Permission.SETUP),
    HelpMessage("delete <name>", "Delete a bedwars game", Permission.SETUP),
    HelpMessage("save <name>", "Save a bedwars game", Permission.SETUP),
    HelpMessage("stop <name>", "Start a bedwars game", Permission.START),
    HelpMessage("stop <name>", "Stop a bedwars game", Permission.STOP),
    HelpMessage("edit <name>", "Prevents a bedwars game from being started (used when editing)", Permission.SETUP),
    HelpMessage("setup <name> ready", "Detects if the game can be saved", Permission.SETUP),
    HelpMessage("setup <name> finish", "Finishes a game setup, also saves it", Permission.SETUP),
    HelpMessage("setup <name> lobby", "Set the lobby location", Permission.SETUP),
    HelpMessage("setup <name> spec", "Set the spectator spot", Permission.SETUP),
    HelpMessage("setup <name> minplayers [min]", "Set the minimum players", Permission.SETUP),
    HelpMessage("setup <name> maxplayers [max]", "Set the maximum players", Permission.SETUP),
    HelpMessage("setup <name> shop add", "Set the villager being looked at as a shop", Permission.SETUP),
    HelpMessage("setup <name> shop remove", "Removes the villager being looked at as a shop", Permission.SETUP),
    HelpMessage("setup <name> upgrades add", "Set the villager being looked at as an upgrade shop", Permission.SETUP),
    HelpMessage("setup <name> upgrades remove", "Removes the villager being looked at as an upgrade shop", Permission.SETUP),
    HelpMessage("setup <name> spawner add <iron/gold/diamond/emerald>", "Add a spawner", Permission.SETUP),
    HelpMessage("setup <name> spawner remove", "Removes a spawner within 5 blocks", Permission.SETUP),
    HelpMessage("setup <name> team", "Get the current teams", Permission.SETUP),
    HelpMessage("setup <name> team add <color>", "Create a team, with the spawn at your location", Permission.SETUP),
    HelpMessage("setup <name> team remove <color>", "Remove a team", Permission.SETUP),
    HelpMessage("setup <name> bed add <color>", "Set the bed of the team color of the bed you are standing on", Permission.SETUP),
    HelpMessage("setup <name> bed remove <color>", "Unsets the bed of the team color", Permission.SETUP),
)

fun CommandSender.showHelpMessage(label: String, page: Int = 1) = showHelpMessage(label, null, page)

fun CommandSender.showHelpMessage(label: String, error: String?, page: Int = 1) {
    sendMessage("${ChatColor.DARK_BLUE}------------------------------------------------")
    sendMessage("${ChatColor.GREEN}Bedwars v${JavaPlugin.getPlugin(BedwarsPlugin::class.java).description.version} " +
            "Help Page: $page/${getMaxHelpPages()}  <> = required  [] = optional")
    val newCommands = commands.filter { msg -> hasPermission(msg.permission) }
    for (i in ((page - 1) * 7) until page * 7) {
        val cmd = newCommands.getSafe(i)?: continue
        sendHelpMsgFormatted(label, cmd)
    }
    error?.let {
        sendMessage("${ChatColor.RED}$it")
    }
    sendMessage("${ChatColor.DARK_BLUE}------------------------------------------------")
}

fun Permissible.getMaxHelpPages(): Int {
    val newCommands = commands.filter { msg -> hasPermission(msg.permission) }
    return ceil(newCommands.size.toDouble() / 7.0).toInt()
}

private fun CommandSender.sendHelpMsgFormatted(label: String, message: HelpMessage) {
    if (!hasPermission(message.permission)) {
        return
    }
    sendMessage("${ChatColor.AQUA}/$label ${message.arg} - ${ChatColor.GOLD}${message.description}")
}

fun List<UUID>.getPlayers() = map(Bukkit::getPlayer).filter(Objects::nonNull)

fun Set<UUID>.getPlayers() = map(Bukkit::getPlayer).filter(Objects::nonNull).toSet()

fun Set<Player>.getUsernames() = map(Player::getName).toSet()

fun CommandSender.sendMessage(message: ErrorMessages) = sendMessage(message.message)

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

fun Permissible.hasPermission(permission: Permission) = hasPermission(permission.permission)

fun Player.playSound(sound: Sound, volume: Float = 1.0f, pitch: Float = 1.0f) {
    playSound(location, sound, volume, pitch)
}

fun LivingEntity.getLookingAt(distance: Double = 4.0): LivingEntity? {
    var closest: Pair<LivingEntity, Double>? = null
    for (entity in getNearbyEntities(distance, distance, distance)) {
        if (entity !is LivingEntity) {
            continue
        }
        if (!isLookingAt(entity)) {
            continue
        }
        val entityDistance = location.distance(entity.location)
        if (closest == null) {
            closest = Pair(entity, entityDistance)
            continue
        }
        if (closest.second > entityDistance) {
            closest = Pair(entity, entityDistance)
        }
    }
    return closest?.first
}

fun LivingEntity.isLookingAt(other: LivingEntity): Boolean {
    val eye = eyeLocation
    val toEntity = other.eyeLocation.toVector().subtract(eye.toVector())
    val dot = toEntity.normalize().dot(eye.direction)
    return dot > 0.99
}

fun Player.sendOtherTitle(
    title: String? = null,
    subtitle: String? = null,
    fadeIn: Int = 10,
    stay: Int = 50,
    fadeOut: Int = 10
) = sendTitle(title, subtitle, fadeIn, stay, fadeOut)

fun Player.sendTitle(
    title: String? = null,
    subtitle: String? = null,
    fadeIn: Int = 10,
    stay: Int = 50,
    fadeOut: Int = 10
) {
    val newTitle = LegacyComponentSerializer.legacySection().deserialize(title ?: "")
    val newSubTitle = LegacyComponentSerializer.legacySection().deserialize(subtitle ?: "")
    val times =
        Title.Times.of(Ticks.duration(fadeIn.toLong()), Ticks.duration(stay.toLong()), Ticks.duration(fadeOut.toLong()))
    val cTitle = Title.title(newTitle, newSubTitle, times)
    val audience = BukkitAudiences.create(JavaPlugin.getPlugin(BedwarsPlugin::class.java)).player(this)
    audience.showTitle(cTitle)
}