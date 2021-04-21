package me.dkim19375.bedwars.plugin.data

import me.dkim19375.bedwars.plugin.enumclass.Team
import me.dkim19375.bedwars.plugin.util.getBedHead
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.material.Bed

@Suppress("DataClassPrivateConstructor")
data class BedData private constructor(val team: Team, val location: Location, val face: BlockFace) : ConfigurationSerializable {
    override fun serialize(): Map<String, Any> =
        mapOf(
            "team" to team.name,
            "location" to location,
            "face" to face.name)

    companion object {
        @Suppress("unused")
        @JvmStatic
        fun deserialize(map: Map<String, Any>): BedData {
            return BedData(
                Team.fromString(map["team"] as String)!!,
                map["location"] as Location,
                BlockFace.valueOf(map["face"] as String)
            )
        }

        fun getBedData(team: Team, block: Block): BedData {
            return BedData(team, block.getBedHead(), (block.state.data as Bed).facing)
        }
    }
}