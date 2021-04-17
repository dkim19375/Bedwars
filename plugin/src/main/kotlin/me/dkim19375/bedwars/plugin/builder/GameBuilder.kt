package me.dkim19375.bedwars.plugin.builder

import me.dkim19375.bedwars.plugin.data.BedData
import me.dkim19375.bedwars.plugin.data.GameData
import me.dkim19375.bedwars.plugin.data.SpawnerData
import me.dkim19375.bedwars.plugin.data.TeamData
import me.dkim19375.bedwars.plugin.enumclass.Team
import org.bukkit.Location
import org.bukkit.World
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
class GameBuilder {
    var displayName: String? = null
    var world: World? = null
    var minPlayers: Int = 2
    var maxPlayers: Int = 8
    var teams: Map<Team, TeamData>? = null
    var shopVillagers: List<UUID>? = null
    var upgradeVillagers: List<UUID>? = null
    var spawners: Set<SpawnerData>? = null
    var beds: Set<BedData>? = null
    var spec: Location? = null

    fun canBuild() =
        (displayName != null) &&
                (world != null) &&
                (teams != null) &&
                (shopVillagers != null) &&
                (upgradeVillagers != null) &&
                (spawners != null) &&
                (beds != null) &&
                (spec != null)

    fun build(): GameData? {
        val displayName = displayName ?: return null
        val world = world ?: return null
        val teams = teams ?: return null
        val shopVillagers = shopVillagers ?: return null
        val upgradeVillagers = upgradeVillagers ?: return null
        val spawners = spawners ?: return null
        val beds = beds ?: return null
        val spec = spec ?: return null
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
            spec
        )
    }


}