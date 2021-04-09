package me.dkim19375.bedwars.plugin.util

import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import java.util.*

private val entities = mutableMapOf<UUID, Entity>()

fun UUID.getEntity(): Entity? {
    val entityInList = entities[this]?: return getEntityFromLoop(this)
    if (!entities.containsKey(this)) {
        return getEntityFromLoop(this)
    }
    if (entityInList.isValid) {
        return entityInList
    }
    entities.remove(this)
    return getEntityFromLoop(this)
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