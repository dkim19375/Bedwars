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

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.manager.BedwarsGame
import me.dkim19375.bedwars.plugin.util.update
import me.dkim19375.dkimcore.extension.toUUID
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.configuration.serialization.ConfigurationSerializable
import java.util.*

data class GameData(
    private val gameWorld: World,
    val minPlayers: Int = 2,
    val maxPlayers: Int = 8,
    private val gameTeams: Set<TeamData>,
    val shopVillagers: Set<UUID>,
    val upgradeVillagers: Set<UUID>,
    private val gameSpawners: Set<SpawnerData>,
    private val gameBeds: Set<BedData>,
    private val gameSpec: Location,
    private val gameLobby: Location
) : ConfigurationSerializable {
    val world: World
        get() =  Bukkit.getWorld(gameWorld.name) ?: gameWorld
    val spec: Location
        get() {
            if (gameSpec.world.name == world.name) {
                return gameSpec.update()
            }
            return gameSpec.setWorld(world.name, false)
        }
    val lobby: Location
        get() {
            if (gameLobby.world.name == world.name) {
                return gameLobby.update()
            }
            return gameLobby.setWorld(world.name, false)
        }
    val teams: Set<TeamData>
        get() {
            if (gameTeams
                    .map(TeamData::spawn)
                    .map(Location::getWorld)
                    .map(World::getName)
                    .none { s -> s != world.name }
            ) {
                return gameTeams
            }
            return gameTeams.map { data ->
                data.copy(
                    team = data.team,
                    spawn = data.spawn.setWorld(world.name, false)
                )
            }.toSet()
        }
    val spawners: Set<SpawnerData>
        get() {
            if (gameSpawners
                    .map(SpawnerData::location)
                    .map(Location::getWorld)
                    .map(World::getName)
                    .none { s -> s != world.name }
            ) {
                return gameSpawners
            }
            return gameSpawners.map { data ->
                data.copy(
                    type = data.type,
                    location = data.location.setWorld(world.name, false)
                )
            }.toSet()
        }
    val beds: Set<BedData>
        get() {
            if (gameBeds
                    .map(BedData::location)
                    .map(Location::getWorld)
                    .map(World::getName)
                    .none { s -> s != world.name }
            ) {
                return gameBeds
            }
            return gameBeds.map { data ->
                data.copy(
                    team = data.team,
                    location = data.location.setWorld(world.name, false),
                    face = data.face
                )
            }.toSet()
        }

    fun save(plugin: BedwarsPlugin) {
        val copy = copy(
            gameWorld = world,
            minPlayers = minPlayers,
            maxPlayers = maxPlayers,
            gameTeams = teams,
            shopVillagers = shopVillagers,
            upgradeVillagers = upgradeVillagers,
            gameSpawners = spawners,
            gameBeds = beds,
            gameSpec = spec,
            gameLobby = lobby
        )
        plugin.dataFileManager.setGameData(copy)
        if (plugin.gameManager.getGame(world) != null) {
            return
        }
        plugin.gameManager.addGame(BedwarsGame(plugin, copy))
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
        "spec" to spec.setWorld(world.name),
        "lobby" to lobby.setWorld(world.name)
    )

    companion object {
        @Suppress("unused", "unchecked_cast")
        @JvmStatic
        fun deserialize(map: Map<String, Any>): GameData {
            val worldName = map["world"] as String
            val world = Bukkit.getWorld(worldName)!!
            return GameData(
                world, // makes sure the world exists
                map["min-players"] as Int,
                map["max-players"] as Int,
                (map["teams"] as List<TeamData>).map { data ->
                    data.copy(
                        team = data.team,
                        spawn = data.spawn.setWorld(worldName)
                    )
                }.toSet(),
                (map["shop-villagers"] as List<String>).mapNotNull(String::toUUID).toSet(),
                (map["upgrade-villagers"] as List<String>).mapNotNull(String::toUUID).toSet(),
                (map["spawners"] as List<SpawnerData>).map { data ->
                    data.copy(
                        type = data.type,
                        location = data.location.setWorld(worldName)
                    )
                }.toSet(),
                (map["beds"] as List<BedData>).map { data ->
                    data.copy(
                        team = data.team,
                        location = data.location.setWorld(worldName),
                        face = data.face
                    )
                }.toSet(),
                (map["spec"] as Location).setWorld(worldName),
                (map["lobby"] as Location).setWorld(worldName)
            )
        }
    }
}

private fun Location.setWorld(world: String, shouldClone: Boolean = true): Location {
    val clone = if (shouldClone) clone() else this
    clone.world = Bukkit.getWorld(world)
    return clone
}