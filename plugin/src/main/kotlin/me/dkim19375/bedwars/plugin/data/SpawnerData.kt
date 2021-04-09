package me.dkim19375.bedwars.plugin.data

import me.dkim19375.bedwars.plugin.enumclass.SpawnerType
import org.bukkit.Location
import org.bukkit.configuration.serialization.ConfigurationSerializable

data class SpawnerData(val type: SpawnerType, val location: Location) : ConfigurationSerializable {
    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf(Pair("type", type.name), Pair("location", location))
    }

    @Suppress("unused")
    constructor(map: Map<String, Any>) :
            this(SpawnerType.valueOf(map["type"] as String), map["location"] as Location)
}
