package me.dkim19375.bedwars.plugin.data

import me.dkim19375.bedwars.plugin.enumclass.Team
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.material.Bed

data class BedData(val team: Team, val location: Location, val face: BlockFace) : ConfigurationSerializable {
    override fun serialize(): Map<String, Any> =
        mapOf(
            "team" to team,
            "location" to location,
            "face" to face.name)

    companion object {
        @Suppress("unused")
        @JvmStatic
        fun deserialize(map: Map<String, Any>): BedData {
            return BedData(
                map["team"] as Team,
                map["location"] as Location,
                BlockFace.valueOf(map["face"] as String)
            )
        }

        fun getBedData(team: Team, block: Block): BedData {
            return BedData(team, block.location, (block.state.data as Bed).facing)
        }
    }
}