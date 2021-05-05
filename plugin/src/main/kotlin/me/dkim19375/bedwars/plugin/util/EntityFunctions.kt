package me.dkim19375.bedwars.plugin.util

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.enumclass.ArmorType
import me.dkim19375.bedwars.plugin.enumclass.ErrorMessages
import me.dkim19375.bedwars.plugin.enumclass.Permission
import me.dkim19375.dkim19375core.data.HelpMessage
import me.dkim19375.dkim19375core.function.filterNonNull
import me.dkim19375.dkim19375core.function.showHelpMessage
import me.dkim19375.dkim19375core.javaplugin.CoreJavaPlugin
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.title.Title
import net.kyori.adventure.util.Ticks
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.permissions.Permissible
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import kotlin.math.ceil

val commands = listOf(
    HelpMessage("help [page]", "Show this help menu", Permission.HELP.permission),
    HelpMessage("list", "List bedwars maps and games", Permission.LIST.permission),
    HelpMessage("join <name>", "Join a bedwars game", Permission.JOIN.permission),
    HelpMessage("quickjoin", "Quickly a bedwars game", Permission.JOIN.permission),
    HelpMessage("leave", "Leave a bedwars game", Permission.LEAVE.permission),
    HelpMessage("reload", "Reload the plugin's config files", Permission.RELOAD.permission),
    HelpMessage("create", "Create a new bedwars game", Permission.SETUP.permission),
    HelpMessage("delete <name>", "Delete a bedwars game", Permission.SETUP.permission),
    HelpMessage("save <name>", "Save a bedwars game", Permission.SETUP.permission),
    HelpMessage("stop <name>", "Start a bedwars game", Permission.START.permission),
    HelpMessage("stop <name>", "Stop a bedwars game", Permission.STOP.permission),
    HelpMessage(
        "edit <name>",
        "Prevents a bedwars game from being started (used when editing.permission)",
        Permission.SETUP.permission
    ),
    HelpMessage(
        "info <name>",
        "See information about a bedwars game",
        Permission.INFO.permission
    ),
    HelpMessage("setup <name> ready", "Detects if the game can be saved", Permission.SETUP.permission),
    HelpMessage("setup <name> finish", "Finishes a game setup, also saves it", Permission.SETUP.permission),
    HelpMessage("setup <name> lobby", "Set the lobby location", Permission.SETUP.permission),
    HelpMessage("setup <name> spec", "Set the spectator spot", Permission.SETUP.permission),
    HelpMessage("setup <name> minplayers [min]", "Set the minimum players", Permission.SETUP.permission),
    HelpMessage("setup <name> maxplayers [max]", "Set the maximum players", Permission.SETUP.permission),
    HelpMessage("setup <name> shop add [tp/teleport]", "Set the villager being looked at as a shop, " +
            "if teleport/tp arg is set, then the villager will teleport to you", Permission.SETUP.permission),
    HelpMessage(
        "setup <name> shop remove",
        "Removes the villager being looked at as a shop",
        Permission.SETUP.permission
    ),
    HelpMessage(
        "setup <name> upgrades add [tp/teleport]",
        "Set the villager being looked at as an upgrade shop, if teleport/tp arg is set, then the villager will teleport to you",
        Permission.SETUP.permission
    ),
    HelpMessage(
        "setup <name> upgrades remove",
        "Removes the villager being looked at as an upgrade shop",
        Permission.SETUP
            .permission
    ),
    HelpMessage("setup <name> spawner add <iron/gold/diamond/emerald>", "Add a spawner", Permission.SETUP.permission),
    HelpMessage("setup <name> spawner remove", "Removes a spawner within 5 blocks", Permission.SETUP.permission),
    HelpMessage("setup <name> team", "Get the current teams", Permission.SETUP.permission),
    HelpMessage(
        "setup <name> team add <color>",
        "Create a team, with the spawn at your location",
        Permission.SETUP.permission
    ),
    HelpMessage("setup <name> team remove <color>", "Remove a team", Permission.SETUP.permission),
    HelpMessage(
        "setup <name> bed add <color>",
        "Set the bed of the team color of the bed you are standing on",
        Permission.SETUP
            .permission
    ),
    HelpMessage("setup <name> bed remove <color>", "Unsets the bed of the team color", Permission.SETUP.permission),
)

fun CommandSender.showHelpMsg(label: String, page: Int = 1) = showHelpMsg(label, null, page)

fun CommandSender.showHelpMsg(label: String, error: ErrorMessages) = showHelpMsg(label, error.message)

fun CommandSender.showHelpMsg(label: String, error: String?, page: Int = 1) {
    showHelpMessage(
        label, error, page, commands,
        JavaPlugin.getProvidingPlugin(BedwarsPlugin::class.java) as CoreJavaPlugin
    )
}

fun Permissible.getMaxHelpPages(): Int {
    val newCommands = commands.filter { msg -> hasPermission(msg.permission) }
    return ceil(newCommands.size.toDouble() / 7.0).toInt()
}

fun PlayerInventory.containsArmor(): ArmorType? {
    for (item in contents) {
        val type = ArmorType.fromMaterial(item.type)
        if (type != null) {
            return type
        }
    }
    return null
}

fun PlayerInventory.containsTool(): Boolean {
    for (item in contents) {
        if (item.type.isTool()) {
            return true
        }
    }
    return false
}

fun PlayerInventory.containsWeapon(): Boolean {
    for (item in contents) {
        if (item.type.isWeapon()) {
            return true
        }
    }
    return false
}

fun Set<UUID>.getPlayers(): Set<Player> = map(Bukkit::getPlayer).filterNonNull().toSet()

fun Set<Player>.getUsernames() = map(Player::getName).toSet()

fun CommandSender.sendMessage(message: ErrorMessages) = sendMessage(message.message)

fun Player.getItemAmount(type: Material): Int {
    var amount = 0
    val inv = inventory.contents.toList()
    for (item in inv) {
        item ?: continue
        if (item.type == type) {
            amount += item.amount
        }
    }
    return amount
}

fun Permissible.hasPermission(permission: Permission) = hasPermission(permission.permission)

fun Player.playSound(sound: Sound, volume: Float = 0.85f, pitch: Float = 1.0f) {
    playSound(location, sound, volume, pitch)
}

fun PlayerInventory.getAllContents(): Array<ItemStack?> {
    return contents.plus(armorContents)
}

fun LivingEntity.getLookingAt(distance: Double = 4.0): LivingEntity? {
    return getTarget(getNearbyEntities(distance, distance, distance)) as? LivingEntity
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

private fun <T : Entity> T.getTarget(
    entities: Iterable<T>
): T? {
    var target: T? = null
    val threshold = 1.0
    for (other in entities) {
        val n = other.location.toVector()
            .subtract(location.toVector())
        if (location.direction.normalize().crossProduct(n)
                .lengthSquared() < threshold
            && n.normalize().dot(
                location.direction.normalize()
            ) >= 0
        ) {
            if (target == null
                || target.location.distanceSquared(
                    location
                ) > other.location
                    .distanceSquared(location)
            ) target = other
        }
    }
    return target
}