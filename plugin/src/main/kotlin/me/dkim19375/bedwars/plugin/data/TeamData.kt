package me.dkim19375.bedwars.plugin.data

import org.bukkit.Location
import org.bukkit.configuration.serialization.ConfigurationSerializable

data class TeamData(val spawn: Location) : ConfigurationSerializable {
    override fun serialize(): Map<String, Any> = mapOf(
        "spawn" to spawn
    )

    companion object {
        @Suppress("unused")
        @JvmStatic
        fun deserialize(map: Map<String, Any>): TeamData = TeamData(map["spawn"] as Location)
    }
}