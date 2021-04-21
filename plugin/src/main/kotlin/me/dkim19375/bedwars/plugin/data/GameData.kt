package me.dkim19375.bedwars.plugin.data

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.manager.BedwarsGame
import me.dkim19375.bedwars.plugin.util.toUUID
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.configuration.serialization.ConfigurationSerializable
import java.util.*

data class GameData(
    val world: World,
    val minPlayers: Int = 2,
    val maxPlayers: Int = 8,
    val teams: Set<TeamData>,
    val shopVillagers: Set<UUID>,
    val upgradeVillagers: Set<UUID>,
    val spawners: Set<SpawnerData>,
    val beds: Set<BedData>,
    val spec: Location,
    val lobby: Location
) : ConfigurationSerializable {

    fun save(plugin: BedwarsPlugin) {
        plugin.dataFileManager.setGameData(this)
        if (plugin.gameManager.getGame(world) != null) {
            return
        }
        plugin.gameManager.addGame(BedwarsGame(plugin, this))
    }

    override fun serialize(): Map<String, Any> = mapOf(
        "world" to world.name,
        "min-players" to minPlayers,
        "max-players" to maxPlayers,
        "teams" to teams.toList(),
        "shop-villagers" to shopVillagers.map(UUID::toString),
        "upgrade-villagers" to upgradeVillagers.map(UUID::toString),
        "spawners" to spawners.toList(),
        "beds" to beds.toList(),
        "spec" to spec,
        "lobby" to lobby
    )

    companion object {
        @Suppress("unused", "unchecked_cast")
        @JvmStatic
        fun deserialize(map: Map<String, Any>): GameData {
            return GameData(
                Bukkit.getWorld(map["world"] as String),
                Integer.valueOf(map["min-players"] as String),
                Integer.valueOf(map["max-players"] as String),
                (map["teams"] as List<TeamData>).toSet(),
                (map["shop-villagers"] as List<String>).map(String::toUUID).filter(Objects::nonNull).map { i -> i!! }
                    .toSet(),
                (map["upgrade-villagers"] as List<String>).map(String::toUUID).filter(Objects::nonNull).map { i -> i!! }
                    .toSet(),
                (map["spawners"] as List<SpawnerData>).toSet(),
                (map["beds"] as List<BedData>).toSet(),
                map["spec"] as Location,
                map["lobby"] as Location
            )
        }
    }
}