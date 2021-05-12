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