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

package me.dkim19375.bedwars.plugin.data

import me.dkim19375.bedwars.api.data.BedwarsBedData
import me.dkim19375.bedwars.api.data.BedwarsGameData
import me.dkim19375.bedwars.api.data.BedwarsSpawnerData
import me.dkim19375.bedwars.api.data.BedwarsTeamData
import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.manager.BedwarsGame
import me.dkim19375.bedwars.plugin.util.update
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import java.util.*

data class GameData(
    @Transient
    private val tempWorld: World,
    val minPlayers: Int = 2,
    val maxPlayers: Int = 8,
    @Transient
    private val tempTeams: Set<TeamData>,
    val shopVillagers: Set<UUID>,
    val upgradeVillagers: Set<UUID>,
    @Transient
    private val tempSpawners: Set<SpawnerData>,
    @Transient
    private val tempBeds: Set<BedData>,
    @Transient
    private val tempSpec: Location,
    @Transient
    private val tempLobby: Location
) : BedwarsGameData {
    val world: World
        get() =  Bukkit.getWorld(tempWorld.name) ?: tempWorld
    val spec: Location
        get() {
            if (tempSpec.world.name == world.name) {
                return tempSpec.update()
            }
            return tempSpec.setWorld(world.name, false)
        }
    val lobby: Location
        get() {
            if (tempLobby.world.name == world.name) {
                return tempLobby.update()
            }
            return tempLobby.setWorld(world.name, false)
        }
    val teams: Set<TeamData>
        get() {
            if (tempTeams
                    .map(TeamData::location)
                    .map(Location::getWorld)
                    .map(World::getName)
                    .none { s -> s != world.name }
            ) {
                return tempTeams
            }
            return tempTeams.map { data ->
                data.copy(
                    team = data.team,
                    location = data.location.setWorld(world.name, false)
                )
            }.toSet()
        }
    val spawners: Set<SpawnerData>
        get() {
            if (tempSpawners
                    .map(SpawnerData::location)
                    .map(Location::getWorld)
                    .map(World::getName)
                    .none { s -> s != world.name }
            ) {
                return tempSpawners
            }
            return tempSpawners.map { data ->
                data.copy(
                    type = data.type,
                    location = data.location.setWorld(world.name, false)
                )
            }.toSet()
        }
    val beds: Set<BedData>
        get() {
            if (tempBeds
                    .map(BedData::location)
                    .map(Location::getWorld)
                    .map(World::getName)
                    .none { s -> s != world.name }
            ) {
                return tempBeds
            }
            return tempBeds.map { data ->
                data.copy(
                    team = data.team,
                    location = data.location.setWorld(world.name, false),
                    face = data.face
                )
            }.toSet()
        }

    fun save(plugin: BedwarsPlugin) {
        val copy = copy(
            tempWorld = world,
            minPlayers = minPlayers,
            maxPlayers = maxPlayers,
            tempTeams = teams,
            shopVillagers = shopVillagers,
            upgradeVillagers = upgradeVillagers,
            tempSpawners = spawners,
            tempBeds = beds,
            tempSpec = spec,
            tempLobby = lobby
        )
        plugin.dataFileManager.setGameData(copy)
        if (plugin.gameManager.getGame(world) != null) {
            return
        }
        plugin.gameManager.addGame(BedwarsGame(plugin, copy))
    }

    override fun getGameWorld(): World = world

    override fun getMinimumPlayers(): Int = minPlayers

    override fun getMaximumPlayers(): Int = maxPlayers

    override fun getGameTeams(): Set<BedwarsTeamData> = teams

    override fun getShopNPCs(): Set<UUID> = shopVillagers

    override fun getUpgradeNPCs(): Set<UUID> = upgradeVillagers

    override fun getGameSpawners(): Set<BedwarsSpawnerData> = spawners

    override fun getGameBeds(): Set<BedwarsBedData> = beds

    override fun getSpectator(): Location = spec

    override fun getGameLobby(): Location = lobby

    override fun clone(
        world: World?,
        minPlayers: Int?,
        maxPlayers: Int?,
        teams: Set<BedwarsTeamData>?,
        shopVillagers: Set<UUID>?,
        upgradeVillagers: Set<UUID>?,
        spawners: Set<BedwarsSpawnerData>?,
        beds: Set<BedwarsBedData>?,
        spec: Location?,
        lobby: Location?
    ): BedwarsGameData = copy(
        tempWorld = world ?: this.world,
        minPlayers = minPlayers ?: this.minPlayers,
        maxPlayers = maxPlayers ?: this.maxPlayers,
        tempTeams = teams?.map { it as TeamData }?.toSet() ?: this.teams,
        shopVillagers = shopVillagers ?: this.shopVillagers,
        upgradeVillagers = upgradeVillagers ?: this.upgradeVillagers,
        tempSpawners = spawners?.map { it as SpawnerData }?.toSet() ?: this.spawners,
        tempBeds = beds?.map { it as BedData }?.toSet() ?: this.beds,
        tempSpec = spec ?: this.spec,
        tempLobby = lobby ?: this.lobby
    )
}

private fun Location.setWorld(world: String, shouldClone: Boolean = true): Location {
    val clone = if (shouldClone) clone() else this
    clone.world = Bukkit.getWorld(world)
    return clone
}