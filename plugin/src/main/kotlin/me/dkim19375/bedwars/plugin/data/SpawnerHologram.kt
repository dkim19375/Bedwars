package me.dkim19375.bedwars.plugin.data

import me.dkim19375.bedwars.plugin.enumclass.SpawnerType
import me.dkim19375.bedwars.plugin.util.getEntity
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import java.util.*

data class SpawnerHologram(
    val type: SpawnerType,
    val spawnTimeStand: UUID,
    val typeArmorStand: UUID,
    val timePos: Location,
    val typePos: Location
) {
    fun getTimeArmorStand(): ArmorStand? = spawnTimeStand.getEntity() as? ArmorStand
    fun getTypeArmorStand(): ArmorStand? = typeArmorStand.getEntity() as? ArmorStand

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SpawnerHologram

        if (type != other.type) return false
        if (typeArmorStand != other.typeArmorStand) return false
        if (spawnTimeStand != other.spawnTimeStand) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + typeArmorStand.hashCode()
        result = 31 * result + spawnTimeStand.hashCode()
        return result
    }
}