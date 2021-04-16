package me.dkim19375.bedwars.plugin.data

import me.dkim19375.bedwars.plugin.enumclass.SpawnerType
import org.bukkit.Location
import org.bukkit.configuration.serialization.ConfigurationSerializable

data class SpawnerData(val type: SpawnerType, val location: Location) : ConfigurationSerializable {
    override fun serialize(): Map<String, Any> = mapOf(
        "type" to type.name,
        "location" to location
    )

    companion object {
        @Suppress("unused")
        @JvmStatic
        fun deserialize(map: Map<String, Any>): SpawnerData {
            return SpawnerData(SpawnerType.valueOf(map["type"] as String), map["location"] as Location)
        }
    }
}
