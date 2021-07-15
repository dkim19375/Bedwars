/*
 * MIT License
 *
 * Copyright (c) 2021 dkim19375
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.dkim19375.bedwars.plugin.data

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.manager.BedwarsGame
import me.dkim19375.bedwars.plugin.util.update
import me.dkim19375.dkimbukkitcore.function.toUUID
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