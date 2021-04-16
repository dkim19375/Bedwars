package me.dkim19375.bedwars.plugin.util

import me.dkim19375.bedwars.plugin.enumclass.ArmorType
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.PlayerInventory
import java.util.*

private val entities = mutableMapOf<UUID, Entity>()

fun UUID.getEntity(): Entity? {
    val entityInList = entities[this] ?: return getEntityFromLoop(this)
    if (!entities.containsKey(this)) {
        return getEntityFromLoop(this)
    }
    if (entityInList.isValid) {
        return entityInList
    }
    entities.remove(this)
    return getEntityFromLoop(this)
}

fun PlayerInventory.hasArmor(vararg types: ArmorType?): Boolean {
    for (item in contents) {
        if (ArmorType.fromMaterial(item.type).containsAny(types.toList())) {
            return true
        }
    }
    return false
}

fun InventoryClickEvent.getPlayer() = view.player as Player

fun PlayerInventory.hasItem(vararg types: Material?): Boolean {
    for (item in contents) {
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

fun Material.isTool() = when (this) {
    Material.WOOD_PICKAXE -> {
        true
    }
    Material.WOOD_AXE -> {
        true
    }
    Material.STONE_PICKAXE -> {
        true
    }
    Material.STONE_AXE -> {
        true
    }
    Material.GOLD_PICKAXE -> {
        true
    }
    Material.GOLD_AXE -> {
        true
    }
    Material.IRON_PICKAXE -> {
        true
    }
    Material.IRON_AXE -> {
        true
    }
    Material.DIAMOND_PICKAXE -> {
        true
    }
    Material.DIAMOND_AXE -> {
        true
    }
    else -> {
        false
    }
}

fun Material.isWeapon() = when (this) {
    Material.WOOD_SWORD -> {
        true
    }
    Material.STONE_SWORD -> {
        true
    }
    Material.GOLD_SWORD -> {
        true
    }
    Material.IRON_SWORD -> {
        true
    }
    Material.DIAMOND_SWORD -> {
        true
    }
    else -> {
        false
    }
}