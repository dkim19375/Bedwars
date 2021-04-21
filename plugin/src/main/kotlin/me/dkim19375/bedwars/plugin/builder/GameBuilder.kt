package me.dkim19375.bedwars.plugin.builder

import me.dkim19375.bedwars.plugin.data.BedData
import me.dkim19375.bedwars.plugin.data.GameData
import me.dkim19375.bedwars.plugin.data.SpawnerData
import me.dkim19375.bedwars.plugin.data.TeamData
import me.dkim19375.bedwars.plugin.enumclass.BuildError
import me.dkim19375.bedwars.plugin.enumclass.Team
import org.bukkit.Location
import org.bukkit.World
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
class GameBuilder(
    var world: World,
    var minPlayers: Int = 2,
    var maxPlayers: Int = 8,
    var teams: MutableMap<Team, TeamData> = mutableMapOf(),
    val shopVillagers: MutableSet<UUID> = mutableSetOf(),
    val upgradeVillagers: MutableSet<UUID> = mutableSetOf(),
    var spawners: MutableSet<SpawnerData> = mutableSetOf(),
    var beds: MutableSet<BedData> = mutableSetOf(),
    var spec: Location? = null,
    var lobby: Location? = null
) {

    fun canBuild(): Set<BuildError> {
        val errors = mutableSetOf<BuildError>()
        spec ?: errors.add(BuildError.SPEC)
        lobby ?: errors.add(BuildError.LOBBY)
        if (teams.isEmpty()) {
            errors.add(BuildError.TEAMS)
        }
        if (shopVillagers.isEmpty()) {
            errors.add(BuildError.SHOP)
        }
        if (upgradeVillagers.isEmpty()) {
            errors.add(BuildError.UPGRADES)
        }
        if (spawners.isEmpty()) {
            errors.add(BuildError.SPAWNERS)
        }
        if (beds.size < teams.size) {
            errors.add(BuildError.NOT_ENOUGH_BEDS)
        }
        return errors.toSet()
    }

    fun build(): GameData? {
        val spec = spec ?: return null
        val lobby = lobby ?: return null
        if (canBuild().isNotEmpty()) {
            return null
        }
        return GameData(
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