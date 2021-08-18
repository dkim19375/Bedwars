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

@file:Suppress("unused")

package me.dkim19375.bedwars.plugin.util

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.enumclass.ArmorType
import me.dkim19375.bedwars.plugin.enumclass.ErrorMessages
import me.dkim19375.bedwars.plugin.enumclass.Permission
import me.dkim19375.dkimbukkitcore.data.HelpMessage
import me.dkim19375.dkimbukkitcore.function.showHelpMessage
import me.dkim19375.dkimbukkitcore.javaplugin.CoreJavaPlugin
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.title.Title
import net.kyori.adventure.util.Ticks
import org.bukkit.Bukkit
import org.bukkit.Location
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
    HelpMessage("setup <name> lobby", "Set the lobby location", Permission.SETUP.permission),
    HelpMessage("setup <name> spec", "Set the spectator spot", Permission.SETUP.permission),
    HelpMessage("setup <name> minplayers [min]", "Set the minimum players", Permission.SETUP.permission),
    HelpMessage("setup <name> maxplayers [max]", "Set the maximum players", Permission.SETUP.permission),
    HelpMessage(
        "setup <name> shop add [tp/teleport]", "Set the villager being looked at as a shop, " +
                "if teleport/tp arg is set, then the villager will teleport to you", Permission.SETUP.permission
    ),
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

fun CommandSender.showHelpMsg(label: String, error: String?, page: Int = 1) = showHelpMessage(
    label, error, page, commands,
    JavaPlugin.getProvidingPlugin(BedwarsPlugin::class.java) as CoreJavaPlugin
)

fun Permissible.getMaxHelpPages(): Int =
    ceil(commands.filter { msg -> hasPermission(msg.permission) }.size.toDouble() / 7.0).toInt()

fun PlayerInventory.containsArmor(): ArmorType? = getAllContents()
    .toList()
    .filterNotNull()
    .map(ItemStack::getType)
    .mapNotNull(ArmorType::fromMaterial)
    .firstOrNull()

fun PlayerInventory.containsTool(): Boolean = getAllContents()
    .toList()
    .filterNotNull()
    .map(ItemStack::getType)
    .any(Material::isTool)

fun PlayerInventory.containsWeapon(): Boolean = getAllContents()
    .toList()
    .filterNotNull()
    .map(ItemStack::getType)
    .any(Material::isWeapon)

fun Set<UUID>.getPlayers(): Set<Player> = mapNotNull(Bukkit::getPlayer).toSet()

fun Set<Player>.getUsernames() = map(Player::getName).toSet()

fun CommandSender.sendMessage(message: ErrorMessages) = sendMessage(message.message)

fun Player.getItemAmount(type: Material): Int {
    var amount = 0
    val inv = inventory.getAllContents().toList()
    for (item in inv) {
        item ?: continue
        if (item.type == type) {
            amount += item.amount
        }
    }
    return amount
}

fun Permissible.hasPermission(permission: Permission) = hasPermission(permission.permission)

fun Player.playSound(sound: Sound, volume: Float = 0.85f, pitch: Float = 1.0f) =
    playSound(location, sound, volume, pitch)

fun PlayerInventory.getAllContents(): List<ItemStack?> = (0..39).map(this::getItem)

fun PlayerInventory.setAllContents(items: List<ItemStack?>) {
    items.forEachIndexed { index, itemStack ->
        setItem(index, itemStack)
    }
}

fun LivingEntity.getLookingAt(distance: Double = 4.0): LivingEntity? =
    getTarget(getNearbyEntities(distance, distance, distance)) as? LivingEntity

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
) = BukkitAudiences.create(JavaPlugin.getPlugin(BedwarsPlugin::class.java)).player(this).showTitle(
    Title.title(
        LegacyComponentSerializer.legacySection().deserialize(title ?: ""),
        LegacyComponentSerializer.legacySection().deserialize(subtitle ?: ""),
        Title.Times.of(Ticks.duration(fadeIn.toLong()), Ticks.duration(stay.toLong()), Ticks.duration(fadeOut.toLong()))
    )
)

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

fun ItemStack.getNewItem(player: Player?): ItemStack {
    player ?: return clone()
    val configItem = getConfigItem() ?: return clone()
    var newItem: ItemStack = clone()
    player.inventory.contents.forEach contentLoop@{ invItem ->
        invItem ?: return@contentLoop
        val invConfigItem = invItem.getConfigItem() ?: return@contentLoop
        if (invConfigItem.name != configItem.name) {
            return@contentLoop
        }
        newItem = invItem.clone()
    }
    newItem.amount = amount
    return newItem
}

fun Player.giveItem(vararg items: ItemStack?) {
    for (item in items) {
        item?.getNewItem(this)?.let { inventory.addItem(it) }
    }
}

fun Entity.teleportUpdated(location: Location): Boolean = teleport(location.update())

fun PlayerInventory.clearAll() {
    for (i in 0..39) {
        setItem(i, ItemStack(Material.AIR))
    }
}