package me.dkim19375.bedwars.plugin.data

import me.dkim19375.bedwars.plugin.enumclass.Team
import org.bukkit.Location
import org.bukkit.configuration.serialization.ConfigurationSerializable

data class TeamData(val team: Team, val spawn: Location) : ConfigurationSerializable {
    override fun serialize(): Map<String, Any> = mapOf(
        "team" to team.name,
        "spawn" to spawn
    )

    companion object {
        @Suppress("unused")
        @JvmStatic
        fun deserialize(map: Map<String, Any>): TeamData = TeamData(Team.fromString(map["team"] as String)!!, map["spawn"] as Location)
    }
}