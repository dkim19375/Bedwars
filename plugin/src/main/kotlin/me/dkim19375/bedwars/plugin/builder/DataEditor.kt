package me.dkim19375.bedwars.plugin.builder

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.manager.BedwarsGame

class DataEditor(private val plugin: BedwarsPlugin, game: BedwarsGame?, builder: GameBuilder?, val world: String) {
    val data: GameBuilder
    val type: GameDataType
    lateinit var game: BedwarsGame
        private set

    init {
        if (game == null) {
            type = GameDataType.BUILDER
            if (builder == null) {
                data = GameBuilder()
            } else {
                data = GameBuilder(
                    builder.world,
                    builder.minPlayers,
                    builder.maxPlayers,
                    builder.teams.toMutableMap(),
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
                game.data.teams.toMutableMap(),
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
            return newData
        }
        val newGameData = newData.build() ?: return newData
        val newGame = BedwarsGame(plugin, newGameData)
        plugin.gameManager.addGame(world, newGame)
        plugin.dataFileManager.setGameData(newGameData)
        return newData
    }

    private fun getBuilder(): GameBuilder {
        return GameBuilder(
            data.world,
            data.minPlayers,
            data.maxPlayers,
            data.teams.toMutableMap(),
            data.shopVillagers.toMutableSet(),
            data.upgradeVillagers.toMutableSet(),
            data.spawners.toMutableSet(),
            data.beds.toMutableSet(),
            data.spec?.clone(),
            data.lobby?.clone()
        )
    }

    enum class GameDataType {
        EXISTING,
        BUILDER
    }
}