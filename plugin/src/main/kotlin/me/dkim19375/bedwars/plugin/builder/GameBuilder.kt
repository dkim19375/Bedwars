/*
 *     Bedwars, a minigame for spigot
 *     Copyright (C) 2021  dkim19375
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.dkim19375.bedwars.plugin.builder

import me.dkim19375.bedwars.plugin.data.BedData
import me.dkim19375.bedwars.plugin.data.GameData
import me.dkim19375.bedwars.plugin.data.SpawnerData
import me.dkim19375.bedwars.plugin.data.TeamData
import me.dkim19375.bedwars.plugin.enumclass.BuildError
import org.bukkit.Location
import org.bukkit.World
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
data class GameBuilder(
    var world: World,
    var minPlayers: Int = 2,
    var maxPlayers: Int = 8,
    var teams: MutableSet<TeamData> = mutableSetOf(),
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

    fun build(force: Boolean = false): GameData? {
        if (canBuild().isNotEmpty() && !force) {
            return null
        }
        return GameData(
            gameWorld = world,
            minPlayers = minPlayers,
            maxPlayers = maxPlayers,
            gameTeams = teams,
            shopVillagers = shopVillagers,
            upgradeVillagers = upgradeVillagers,
            gameSpawners = spawners,
            gameBeds = beds,
            gameSpec = spec ?: run {
                if (!force) {
                    return null
                }
                Location(world, 0.0, 0.0, 0.0)
            },
            gameLobby = lobby ?: run {
                if (!force) {
                    return null
                }
                Location(world, 0.0, 0.0, 0.0)
            }
        )
    }
}