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

import me.dkim19375.bedwars.api.enumclass.Team
import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.data.ItemWrapper
import me.dkim19375.bedwars.plugin.enumclass.ArmorType
import me.dkim19375.dkimbukkitcore.data.LocationWrapper
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.material.Bed
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.Potion
import java.util.*
import kotlin.math.abs

private val entities = mutableMapOf<UUID, Entity>()
private val plugin: BedwarsPlugin by lazy { JavaPlugin.getPlugin(BedwarsPlugin::class.java) }

fun UUID.getEntity(): Entity? {
    val entityInList = entities[this] ?: run {
        getEntityFromLoop(this)
        return getEntity()
    }
    if (entityInList.isValid) {
        return entityInList
    }
    entities.remove(this)
    return getEntity()
}

fun PlayerInventory.hasArmor(vararg types: ArmorType?): Boolean {
    for (item in contents) {
        item ?: continue
        if (ArmorType.fromMaterial(item.type).containsAny(types.toList())) {
            return true
        }
    }
    return false
}

fun InventoryClickEvent.getPlayer() = view.player as Player

fun PlayerInventory.hasItem(vararg types: Material?): Boolean {
    for (item in contents) {
        item ?: continue
        if (types.contains(item.type)) {
            return true
        }
    }
    return false
}

private fun getEntityFromLoop(uuid: UUID): Entity? {
    for (world in Bukkit.getServer().worlds) {
        for (entity in world.entities) {
            if (entity.uniqueId == uuid) {
                entities[uuid] = entity
                return entity
            }
            entities[uuid] = entity
        }
    }
    return null
}

fun String.loadWorld(worldCreator: WorldCreator? = null): Pair<Boolean, World?> {
    plugin.worldManager?.let {
        return Pair(it.loadWorld(this), Bukkit.getWorld(this))
    }
    worldCreator?.environment(World.Environment.NORMAL)
    val world = worldCreator?.createWorld()
    return Pair(world != null, world)
}

fun WorldCreator.loadWorld(): Pair<Boolean, World?> = name().loadWorld(this)

fun String.unloadWorld(): Boolean {
    return plugin.worldManager?.unloadWorld(this, true) ?: Bukkit.unloadWorld(this, true)
}

fun Material.isArmor() = when (this) {
    Material.LEATHER_HELMET -> true
    Material.LEATHER_CHESTPLATE -> true
    Material.LEATHER_LEGGINGS -> true
    Material.LEATHER_BOOTS -> true
    Material.CHAINMAIL_HELMET -> true
    Material.CHAINMAIL_CHESTPLATE -> true
    Material.CHAINMAIL_LEGGINGS -> true
    Material.CHAINMAIL_BOOTS -> true
    Material.IRON_HELMET -> true
    Material.IRON_CHESTPLATE -> true
    Material.IRON_LEGGINGS -> true
    Material.IRON_BOOTS -> true
    Material.DIAMOND_HELMET -> true
    Material.DIAMOND_CHESTPLATE -> true
    Material.DIAMOND_LEGGINGS -> true
    Material.DIAMOND_BOOTS -> true
    else -> false
}

fun ItemStack.toPotion(): Potion? = try {
    Potion.fromItemStack(this)
} catch (_: IllegalArgumentException) {
    null
}

fun ItemStack.getWrapper(potionDuration: Int = 900, configItem: String? = null): ItemWrapper {
    val potion = toPotion()
    if (potion != null) {
        return ItemWrapper(
            material = type,
            amount = amount,
            potionType = potion.type,
            configItem = configItem,
            potionAmplifier = potion.level + 1,
            potionDuration = potionDuration,
            enchants = enchantments.toMap()
        )
    }
    return ItemWrapper(type, amount, null, configItem, 1, potionDuration, enchantments.toMap())
}

fun Material.isTool() = when (this) {
    Material.WOOD_PICKAXE -> true
    Material.WOOD_AXE -> true
    Material.STONE_PICKAXE -> true
    Material.STONE_AXE -> true
    Material.GOLD_PICKAXE -> true
    Material.GOLD_AXE -> true
    Material.IRON_PICKAXE -> true
    Material.IRON_AXE -> true
    Material.DIAMOND_PICKAXE -> true
    Material.DIAMOND_AXE -> true
    Material.SHEARS -> true
    else -> false
}

fun Material.isWeapon() = when (this) {
    Material.WOOD_SWORD -> true
    Material.STONE_SWORD -> true
    Material.GOLD_SWORD -> true
    Material.IRON_SWORD -> true
    Material.DIAMOND_SWORD -> true
    else -> false
}

fun Location.getOppositeYaw(): Location {
    val clone = clone()
    clone.yaw = clone.yaw - 180
    if (clone.yaw < 0) {
        clone.yaw = 360 - abs(clone.yaw)
    }
    return clone
}

fun Location.update(): Location = Location(Bukkit.getWorld(world?.name), x, y, z, yaw, pitch)

fun Location?.getSafeDistance(other: Location?): Double {
    this ?: return Double.MAX_VALUE
    other ?: return Double.MAX_VALUE
    if (world.name != other.world.name) {
        return Double.MAX_VALUE
    }
    return try {
        distance(
            if (world.uid != other.world.uid) {
                Location(world, other.x, other.y, other.z, other.yaw, other.pitch)
            } else {
                other
            }
        )
    } catch (_: IllegalArgumentException) {
        Double.MAX_VALUE
    }
}

fun Location.dropItem(item: ItemStack): Item = world.dropItem(this, item)

fun Block.getBedHead(): Location {
    val data = state.data
    if (data !is Bed) {
        return location
    }
    if (data.isHeadOfBed) {
        return location
    }
    val direction = data.facing
    return getRelative(direction).location
}

fun Block.getBedFeet(): Location {
    val data = state.data
    if (data !is Bed) {
        return location
    }
    if (!data.isHeadOfBed) {
        return location
    }
    val direction = data.facing
    return getRelative(direction.oppositeFace).location
}

private val bedMaterials = setOf(Material.BED, Material.BED_BLOCK)

fun Material.isBed(): Boolean = this in bedMaterials

fun Location.format(): String = "${if (world != null) "world: ${world.name}, " else ""}($x, $y, $z)"

fun LocationWrapper.format(): String = "${"world: ${world.name}, "}($x, $y, $z)"

fun ConfigurationSection.getIntOrNull(path: String): Int? = if (isSet(path) && isInt(path)) getInt(path) else null

fun Player.kickPlayerFromWorld() =
    player.teleport(plugin.dataFileManager.getLobby() ?: Bukkit.getWorld(plugin.mainWorld).spawnLocation)

fun ItemMeta.addAllFlags() = ItemFlag.values().toList().forEach { addItemFlags(it) }

fun Team.getColored(material: Material): ItemStack = getColored(ItemStack(material))

fun Team.getColored(item: ItemStack): ItemStack = item.getWrapper().toItemStack(color)

fun checkAsync(reason: String) {
    if (!Bukkit.isPrimaryThread()) {
        throw IllegalStateException("Asynchronous $reason! (Bedwars)")
    }
}