package me.dkim19375.bedwars.plugin.data

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.enumclass.Team
import me.dkim19375.bedwars.plugin.util.toUUID
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.configuration.serialization.ConfigurationSerializable
import java.util.*

data class GameData (
    val displayName: String,
    val world: World,
    val minPlayers: Int = 2,
    val maxPlayers: Int = 8,
    val teams: Set<Team>,
    val shopVillagers: List<UUID>,
    val upgradeVillagers: List<UUID>,
    val spawners: Set<SpawnerData>,
    val beds: Set<BedData>
) : ConfigurationSerializable {

    fun save(plugin: BedwarsPlugin) = plugin.dataFileManager.setGameData(this)

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
        map["beds"] = beds.toList()
        return map
    }

    companion object {
        @Suppress("unused", "unchecked_cast")
        @JvmStatic
        fun deserialize(map: Map<String, Any>): GameData {
            return GameData(
                map["display-name"] as String,
                Bukkit.getWorld(map["world"] as String),
                Integer.valueOf(map["min-players"] as String),
                Integer.valueOf(map["max-players"] as String),
                (map["teams"] as List<Team>).toSet(),
                (map["shop-villagers"] as List<String>).map(String::toUUID).toMutableList() as MutableList<UUID>,
                (map["upgrade-villagers"] as List<String>).map(String::toUUID).toMutableList() as MutableList<UUID>,
                (map["spawners"] as List<SpawnerData>).toSet(),
                (map["beds"] as List<BedData>).toSet()
            )
        }
    }
}