package me.dkim19375.bedwars.plugin.builder

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.data.BedData
import me.dkim19375.bedwars.plugin.data.GameData
import me.dkim19375.bedwars.plugin.data.SpawnerData
import me.dkim19375.bedwars.plugin.data.TeamData
import me.dkim19375.bedwars.plugin.enumclass.Team
import org.bukkit.Location
import org.bukkit.World
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
class GameBuilder(private val plugin: BedwarsPlugin) {
    var displayName: String? = null
    var world: World? = null
    var minPlayers: Int = 2
    var maxPlayers: Int = 8
    var teams: MutableMap<Team, TeamData> = mutableMapOf()
    val shopVillagers: MutableList<UUID> = mutableListOf()
    val upgradeVillagers: MutableList<UUID> = mutableListOf()
    var spawners: MutableSet<SpawnerData> = mutableSetOf()
    var beds: MutableSet<BedData> = mutableSetOf()
    var spec: Location? = null
    var lobby: Location? = null

    fun canBuild() =
        (displayName != null) &&
                (world != null) &&
                (teams.isEmpty()) &&
                (shopVillagers.isEmpty()) &&
                (upgradeVillagers.isEmpty()) &&
                (spawners.isEmpty()) &&
                (beds.isEmpty()) &&
                (spec != null) &&
                (lobby != null)

    fun build(): GameData? {
        val displayName = displayName ?: return null
        val world = world ?: return null
        val spec = spec ?: return null
        val lobby = lobby ?: return null
        return GameData(
            displayName,
            world,
            minPlayers,
            maxPlayers,
            teams,
            shopVillagers,
            upgradeVillagers,
            spawners,
            beds,
            spec,
            lobby
        )
    }


}