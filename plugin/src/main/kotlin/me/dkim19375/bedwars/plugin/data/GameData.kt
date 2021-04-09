package me.dkim19375.bedwars.plugin.data

import me.dkim19375.bedwars.plugin.enumclass.Team
import me.dkim19375.bedwars.plugin.util.toUUID
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.configuration.serialization.ConfigurationSerializable
import java.util.*

data class GameData(
    var displayName: String,
    var world: World,
    var minPlayers: Int = 2,
    var maxPlayers: Int = 8,
    var teams: Set<Team>,
    val shopVillagers: MutableList<UUID>,
    val upgradeVillagers: MutableList<UUID>,
    val spawners: Set<SpawnerData>
) : ConfigurationSerializable {

    override fun serialize(): MutableMap<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["display-name"] = displayName
        map["world"] = world.name
        map["min-players"] = minPlayers
        map["max-players"] = maxPlayers
        map["teams"] = teams.toList()
        map["shop-villagers"] = shopVillagers.map(UUID::toString)
        map["upgrade-villagers"] = upgradeVillagers.map(UUID::toString)
        map["spawners"] = spawners.toList()
        return map
    }

    companion object {
        @Suppress("unused", "unchecked_cast")
        fun deserialize(map: Map<String, Any>): GameData {
            return GameData(
                map["display-name"] as String,
                Bukkit.getWorld(map["world"] as String),
                Integer.valueOf(map["min-players"] as String),
                Integer.valueOf(map["max-players"] as String),
                (map["teams"] as List<Team>).toSet(),
                (map["shop-villagers"] as List<String>).map(String::toUUID).toMutableList() as MutableList<UUID>,
                (map["upgrade-villagers"] as List<String>).map(String::toUUID).toMutableList() as MutableList<UUID>,
                (map["spawners"] as List<SpawnerData>).toSet()
            )
        }
    }
}