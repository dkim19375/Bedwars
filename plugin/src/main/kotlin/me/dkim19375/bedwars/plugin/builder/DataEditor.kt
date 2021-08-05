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

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.enumclass.GameState
import me.dkim19375.bedwars.plugin.manager.BedwarsGame
import me.dkim19375.bedwars.plugin.util.getIgnoreCase
import org.bukkit.World

class DataEditor(private val plugin: BedwarsPlugin, game: BedwarsGame?, builder: GameBuilder?, val world: World) {
    val data: GameBuilder
    val type: GameDataType
    lateinit var game: BedwarsGame
        private set

    init {
        if (game == null) {
            type = GameDataType.BUILDER
            if (builder == null) {
                data = GameBuilder(world)
            } else {
                data = GameBuilder(
                    builder.world,
                    builder.minPlayers,
                    builder.maxPlayers,
                    builder.teams.toMutableSet(),
                    builder.shopVillagers.toMutableSet(),
                    builder.upgradeVillagers.toMutableSet(),
                    builder.spawners.toMutableSet(),
                    builder.beds.toMutableSet(),
                    builder.spec?.clone(),
                    builder.lobby?.clone()
                )
            }
        } else {
            this.game = game
            type = GameDataType.EXISTING
            data = GameBuilder(
                game.data.world,
                game.data.minPlayers,
                game.data.maxPlayers,
                game.data.teams.toMutableSet(),
                game.data.shopVillagers.toMutableSet(),
                game.data.upgradeVillagers.toMutableSet(),
                game.data.spawners.toMutableSet(),
                game.data.beds.toMutableSet(),
                game.data.spec.clone(),
                game.data.lobby.clone()
            )
        }
    }

    fun save(): GameBuilder {
        val newData = getBuilder()
        if (type != GameDataType.EXISTING) {
            plugin.gameManager.builders[world.name] = newData
            return newData
        }
        val newGameData = newData.build() ?: return newData
        val newGame = BedwarsGame(plugin, newGameData)
        newGame.state = GameState.LOBBY
        plugin.gameManager.addGame(newGame)
        plugin.dataFileManager.setGameData(newGameData)
        return newData
    }

    private fun getBuilder(): GameBuilder {
        return GameBuilder(
            data.world,
            data.minPlayers,
            data.maxPlayers,
            data.teams.toMutableSet(),
            data.shopVillagers.toMutableSet(),
            data.upgradeVillagers.toMutableSet(),
            data.spawners.toMutableSet(),
            data.beds.toMutableSet(),
            data.spec?.clone(),
            data.lobby?.clone()
        )
    }

    companion object {
        fun findFromWorld(world: World, plugin: BedwarsPlugin): DataEditor? {
            val builder = plugin.gameManager.builders.getIgnoreCase(world.name)
            val game = plugin.gameManager.getGame(world)
            if (builder == null) {
                if (game == null) {
                    return null
                }
                return DataEditor(plugin, game, null, world)
            }
            return DataEditor(plugin, null, builder, world)
        }
    }

    enum class GameDataType {
        EXISTING,
        BUILDER
    }
}