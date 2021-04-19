package me.dkim19375.bedwars.plugin.manager

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.builder.GameBuilder
import me.dkim19375.bedwars.plugin.data.GameData
import me.dkim19375.bedwars.plugin.enumclass.GameState
import me.dkim19375.bedwars.plugin.enumclass.Team
import me.dkim19375.bedwars.plugin.util.getKeyFromStr
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffectType
import org.jetbrains.annotations.Contract
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
class GameManager(private val plugin: BedwarsPlugin) {
    private val games = mutableMapOf<String, BedwarsGame>()
    val builders = mutableMapOf<String, GameBuilder>()
    val invisPlayers = mutableSetOf<UUID>()
    val explosives = mutableMapOf<UUID, UUID>()
    //                        explosive  player

    init {
        Bukkit.getScheduler().runTaskTimer(plugin, {
            for (uuid in getAllPlayers().toSet()) {
                val player = Bukkit.getPlayer(uuid) ?: continue
                if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    invisPlayers.add(player.uniqueId)
                    continue
                }
                invisPlayers.remove(player.uniqueId)
            }
        }, 20L, 20L)
        Bukkit.getScheduler().runTask(plugin, this::reloadData)
    }

    fun save() {
        for (game in getGames().values) {
            plugin.dataFileManager.setGameData(game.data)
        }
    }

    fun reloadData() {
        for (game in getRunningGames().values) {
            game.broadcast("${ChatColor.RED}Reloading data, game stopped.")
            game.forceStop()
        }
        val dataSet = plugin.dataFileManager.getGameDatas()
        games.clear()
        for (data in dataSet) {
            val game = BedwarsGame(plugin, data)
            if (plugin.dataFileManager.isEditing(data)) {
                game.state = GameState.STOPPED
            }
            games[data.world.name] = game
        }
    }

    fun startGame(game: String, force: Boolean) {
        if (isGameRunning(game)) {
            return
        }
        val bwGame = games[game] ?: return
        bwGame.start(force)
    }

    fun forceStopGame(game: String) {
        if (!isGameRunning(game)) {
            return
        }
        val bwGame = games[game] ?: return
        bwGame.forceStop()
    }

    fun isGameRunning(game: String): Boolean {
        return (games[games.getKeyFromStr(game)] ?: return false).isRunning()
    }

    @Contract(pure = true, value = "null -> false")
    fun isGameRunning(world: World?): Boolean {
        world ?: return false
        val gameWorld = getGame(world) ?: return false
        return isGameRunning(gameWorld)
    }

    fun getGame(world: World): String? {
        for (entry in games.entries) {
            val gameWorld = entry.value.data.world
            if (world.uid == gameWorld.uid) {
                return entry.key
            }
        }
        return null
    }

    fun getGame(player: UUID): BedwarsGame? {
        val gameName = getPlayerInGame(player) ?: return null
        return getGame(gameName)
    }

    fun getGame(player: Player): BedwarsGame? = getGame(player.uniqueId)

    fun getGame(name: String): BedwarsGame? {
        games.forEach { (gName, game) ->
            if (gName.equals(name, ignoreCase = true)) {
                return game
            }
        }
        return null
    }

    fun getGames(): Map<String, BedwarsGame> = games.toMap()

    fun addGame(world: String, game: BedwarsGame) {
        games[world] = game
    }

    fun deleteGame(game: BedwarsGame) = deleteGame(game.data)

    fun deleteGame(game: GameData) {
        games.remove(game.world.name)
        plugin.dataFileManager.removeGameData(game)
    }

    fun getRunningGames(): Map<String, BedwarsGame> {
        val map = mutableMapOf<String, BedwarsGame>()
        for (entry in games.entries) {
            if (entry.value.isRunning()) {
                map[entry.key] = entry.value
            }
        }
        return map.toMap()
    }

    fun getTeamOfPlayer(player: Player): Team? {
        val gameName = getPlayerInGame(player) ?: return null
        val game = getGame(gameName) ?: return null
        return game.getTeamOfPlayer(player)
    }

    fun getPlayerInGame(player: Player): String? = getPlayerInGame(player.uniqueId)

    fun getPlayerInGame(player: UUID): String? {
        getGames().forEach { (name, game) ->
            if (game.getTeamOfPlayer(player) != null) {
                return name
            }
        }
        return null
    }

    fun getAllPlayers(): Set<UUID> {
        val set = mutableSetOf<UUID>()
        getGames().forEach { (_, game) ->
            set.addAll(game.getPlayersInGame())
        }
        return set.toSet()
    }
}