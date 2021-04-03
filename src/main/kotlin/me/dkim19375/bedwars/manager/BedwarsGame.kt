package me.dkim19375.bedwars.manager

import me.dkim19375.bedwars.BedwarsPlugin
import me.dkim19375.bedwars.util.GameState
import me.dkim19375.bedwars.util.Team
import me.dkim19375.bedwars.util.formatWithColors
import me.dkim19375.bedwars.util.getCombinedValues
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

@Suppress("JoinDeclarationAndAssignment", "MemberVisibilityCanBePrivate")
class BedwarsGame(var world: World,
                  private val plugin: BedwarsPlugin,
                  val minPlayers: Int = 2,
                  val maxPlayers: Int = 8,
                  val teams: Int = 4) {
    var state = GameState.LOBBY
        private set
    var countdown = 10 * 20
    var time = 0
    val players = mutableMapOf<Team, MutableSet<UUID>>()
    val playersInLobby = mutableSetOf<UUID>()
    lateinit var task: BukkitTask
    val beds = mutableMapOf<Team, Boolean>()

    fun start(force: Boolean): Result {
        val result = canStart(force)
        if (result != Result.SUCCESS) {
            return result
        }
        state = GameState.STARTING
        task = object : BukkitRunnable() {
            override fun run() {
                if (countdown < 1) {
                    broadcast("${ChatColor.AQUA}The game started!")
                    cancel()
                    setupAfterStart()
                    return
                }
                broadcast("${ChatColor.GREEN}Starting in ${ChatColor.GOLD}$countdown")
                countdown--
            }
        }.runTaskTimer(plugin, 20L, 20L)
        return Result.SUCCESS
    }

    private fun setupAfterStart() {

    }

    fun stop(winner: Player?, team: Team) {
        Bukkit.broadcastMessage("${winner?.displayName?.formatWithColors(team.color) 
            ?: team.displayName.formatWithColors(team.color)} has won BedWars!")
        players.clear()
        playersInLobby.clear()
        state = GameState.STOPPED
        regenerateMap()
    }

    fun forceStop() {
        Bukkit.broadcastMessage("The bedwars game has been force stopped!")
        players.clear()
        playersInLobby.clear()
        state = GameState.STOPPED
        regenerateMap()
    }

    fun canStart(force: Boolean): Result {
        updatePlayers()
        if (isRunning()) {
            return Result.GAME_RUNNING
        }
        if (plugin.gameManager.isGameRunning(world)) {
            return Result.GAME_IN_WORLD
        }
        if (!force && players.values.size < minPlayers) {
            return Result.NOT_ENOUGH_PLAYERS
        }
        if (state == GameState.REGENERATING_WORLD) {
            return Result.REGENERATING_WORLD
        }
        return Result.SUCCESS
    }

    fun isRunning() = state.running

    fun updatePlayers() {
        for (team in players.keys.toList()) {
            for (player in players.getOrDefault(team, setOf()).toList()) {
                if (Bukkit.getPlayer(player) != null) continue
                players.getOrDefault(team, mutableSetOf()).remove(player)
            }
        }
    }

    fun addPlayer(player: Player): Result {
        if (state != GameState.STARTING) {
            if (isRunning()) {
                return Result.GAME_RUNNING
            }
        }
        if (playersInLobby.size >= maxPlayers) {
            return Result.TOO_MANY_PLAYERS
        }
        playersInLobby.add(player.uniqueId)
        broadcast("${player.displayName}${ChatColor.GREEN} has joined the game! ${playersInLobby.size}/$maxPlayers")
        if (playersInLobby.size >= minPlayers) {
            start(false)
        }
        return Result.SUCCESS
    }

    fun broadcast(text: String) {
        if (state == GameState.LOBBY || state == GameState.STARTING) {
            for (uuid in playersInLobby) {
                val p = Bukkit.getPlayer(uuid)?: continue
                p.sendMessage(text)
            }
            return
        }
        if (!isRunning()) {
            return
        }
        for (uuid in players.values.getCombinedValues()) {
            val p = Bukkit.getPlayer(uuid)?: continue
            p.sendMessage(text)
        }
    }

    fun leavePlayer(player: Player) {
        if (state == GameState.LOBBY || state == GameState.STARTING) {
            if (!playersInLobby.contains(player.uniqueId)) {
                return
            }
            playersInLobby.remove(player.uniqueId)
            broadcast("${player.displayName}${ChatColor.RED} has left the game! ${playersInLobby.size}/$maxPlayers")
            if (playersInLobby.size < minPlayers) {
                task.cancel()
                broadcast("${ChatColor.RED}Cancelled - Not enough players to start!")
            }
            return
        }
        if (state == GameState.STARTED) {
            if (getTeamOfPlayer(player) == null) {
                return
            }
            return
        }
    }

    fun getPlayersInGame(): Set<UUID> {
        if (state == GameState.LOBBY || state == GameState.STARTING) {
            return playersInLobby.toSet()
        }
        if (state == GameState.STARTED) {
            return players.values.getCombinedValues().toSet()
        }
        return setOf()
    }

    fun getTeamOfPlayer(player: Player): Team? {
        players.forEach { (team, players) ->
            if (players.contains(player.uniqueId)) {
                return team
            }
        }
        return null
    }

    fun saveMap() {
        val folder = world.worldFolder
        val originalCreator = WorldCreator(world.name).copy(world)
        Bukkit.unloadWorld(world, true)
        val savedWorld = Paths.get(plugin.dataFolder.absolutePath, "worlds", world.name).toFile()
        Bukkit.getScheduler().runTaskAsynchronously(plugin) {
            savedWorld.delete()
            Files.copy(folder.toPath(), savedWorld.toPath(), StandardCopyOption.REPLACE_EXISTING)
            Bukkit.getScheduler().runTask(plugin, originalCreator::createWorld)
        }
    }

    fun getPlayersInTeam(team: Team): Set<UUID> {
        return players.getOrDefault(team, setOf())
    }

    fun regenerateMap() {
        if (state != GameState.STOPPED) {
            return
        }
        val dir = Paths.get(plugin.dataFolder.absolutePath, "worlds", world.name).toFile()
        if (!dir.exists()) {
            // Something went wrong here
            throw RuntimeException("The directory for the world: ${world.name} doesn't exist! (${dir.absolutePath})")
        }
        state = GameState.REGENERATING_WORLD
        val folder = world.worldFolder
        val originalCreator = WorldCreator(world.name).copy(world)
        Bukkit.unloadWorld(world, true)
        Bukkit.getScheduler().runTaskAsynchronously(plugin) {
            folder.delete()
            Files.copy(dir.toPath(), folder.toPath(), StandardCopyOption.REPLACE_EXISTING)
            Bukkit.getScheduler().runTask(plugin) {
                world = originalCreator.createWorld()
                state = GameState.STOPPED
            }
        }
    }

    // DURING GAME EVENTS

    fun bedBreak(team: Team, player: Player) {
        val teamOfPlayer = getTeamOfPlayer(player)?: return
        broadcast("${team.displayName.formatWithColors(team.color)}'s bed was broken by " +
                "${player.displayName.formatWithColors(teamOfPlayer.color)}!")
    }

    enum class Result {
        SUCCESS,
        GAME_RUNNING,
        GAME_IN_WORLD,
        NOT_ENOUGH_PLAYERS,
        REGENERATING_WORLD,
        TOO_MANY_PLAYERS
    }
}