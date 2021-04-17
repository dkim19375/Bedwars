package me.dkim19375.bedwars.plugin.manager

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.data.GameData
import me.dkim19375.bedwars.plugin.data.LocationWrapper
import me.dkim19375.bedwars.plugin.enumclass.GameState
import me.dkim19375.bedwars.plugin.enumclass.SpawnerType
import me.dkim19375.bedwars.plugin.enumclass.Team
import me.dkim19375.bedwars.plugin.enumclass.formatWithColors
import me.dkim19375.bedwars.plugin.util.getCombinedValues
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.WorldCreator
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

@Suppress("JoinDeclarationAndAssignment", "MemberVisibilityCanBePrivate")
class BedwarsGame(private val plugin: BedwarsPlugin, data: GameData) {
    var state = GameState.LOBBY
        private set
    var countdown = 10 * 20
    var time: Long = 0
    val players = mutableMapOf<Team, MutableSet<UUID>>()
    val playersInLobby = mutableSetOf<UUID>()
    var task: BukkitTask? = null
    private val worldName: String = data.world.name!!
    val beds = mutableMapOf<Team, Boolean>()
    val npcManager = NPCManager(plugin, data)
    val upgradesManager = UpgradesManager(plugin, this)
    val spawnerManager = SpawnerManager(plugin, this)
    val placedBlocks = mutableSetOf<LocationWrapper>()
    var data = data
        private set
        get() {
            return plugin.dataFileManager.getGameData(worldName)!!
        }

    init {
        npcManager.disableAI()
    }

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
        updatePlayers()
        upgradesManager.resetTask()
        var i = 1
        val teams = data.teams.toList()
        for (uuid in playersInLobby.shuffled()) {
            val player = Bukkit.getPlayer(uuid)?: continue
            val teamData = teams[i % teams.size]
            val team = teamData.first
            val set = players.getOrDefault(team, mutableSetOf())
            set.add(player.uniqueId)
            players[team] = set
            i++
        }
        time = System.currentTimeMillis()
        spawnerManager.start()
    }

    fun stop(winner: Player?, team: Team) {
        Bukkit.broadcastMessage(
            "${
                winner?.displayName?.formatWithColors(team.color)
                    ?: team.displayName.formatWithColors(team.color)
            } has won BedWars!"
        )
        forceStop()
    }

    fun forceStop() {
        Bukkit.broadcastMessage("The bedwars game has been force stopped!")
        players.clear()
        playersInLobby.clear()
        task?.cancel()
        task = null
        beds.clear()
        time = 0
        state = GameState.STOPPED
        spawnerManager.runnable?.cancel()
        upgradesManager.stop()
        placedBlocks.clear()
        regenerateMap()
    }

    fun canStart(force: Boolean): Result {
        updatePlayers()
        if (isRunning()) {
            return Result.GAME_RUNNING
        }
        if (plugin.gameManager.isGameRunning(data.world)) {
            return Result.GAME_IN_WORLD
        }
        if (!force && players.values.size < data.minPlayers) {
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
        if (playersInLobby.size >= data.maxPlayers) {
            return Result.TOO_MANY_PLAYERS
        }
        playersInLobby.add(player.uniqueId)
        broadcast("${player.displayName}${ChatColor.GREEN} has joined the game! ${playersInLobby.size}/${data.maxPlayers}")
        if (playersInLobby.size >= data.minPlayers) {
            start(false)
        }
        return Result.SUCCESS
    }

    fun broadcast(text: String) {
        if (state == GameState.LOBBY || state == GameState.STARTING) {
            for (uuid in playersInLobby) {
                val p = Bukkit.getPlayer(uuid) ?: continue
                p.sendMessage(text)
            }
            return
        }
        if (!isRunning()) {
            return
        }
        for (uuid in players.values.getCombinedValues()) {
            val p = Bukkit.getPlayer(uuid) ?: continue
            p.sendMessage(text)
        }
    }

    fun playerKilled(player: Player) {
        val team = getTeamOfPlayer(player)?: return
        if (beds.getOrDefault(team, false)) {
            return
        }

    }

    fun leavePlayer(player: Player) {
        if (state == GameState.LOBBY || state == GameState.STARTING) {
            if (!playersInLobby.contains(player.uniqueId)) {
                return
            }
            playersInLobby.remove(player.uniqueId)
            broadcast("${player.displayName}${ChatColor.RED} has left the game! ${playersInLobby.size}/${data.maxPlayers}")
            if (playersInLobby.size < data.minPlayers) {
                task?.cancel()
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

    fun getTeamOfPlayer(player: Player): Team? = getTeamOfPlayer(player.uniqueId)

    fun getTeamOfPlayer(player: UUID): Team? {
        players.forEach { (team, players) ->
            if (players.contains(player)) {
                return team
            }
        }
        return null
    }

    fun saveMap() {
        val folder = data.world.worldFolder
        val originalCreator = WorldCreator(data.world.name).copy(data.world)
        Bukkit.unloadWorld(data.world, true)
        val savedWorld = Paths.get(plugin.dataFolder.absolutePath, "worlds", data.world.name).toFile()
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
        val dir = Paths.get(plugin.dataFolder.absolutePath, "worlds", data.world.name).toFile()
        if (!dir.exists()) {
            // Something went wrong here
            throw RuntimeException("The directory for the world: ${data.world.name} doesn't exist! (${dir.absolutePath})")
        }
        state = GameState.REGENERATING_WORLD
        val folder = data.world.worldFolder
        val originalCreator = WorldCreator(data.world.name).copy(data.world)
        Bukkit.unloadWorld(data.world, true)
        Bukkit.getScheduler().runTaskAsynchronously(plugin) {
            folder.delete()
            Files.copy(dir.toPath(), folder.toPath(), StandardCopyOption.REPLACE_EXISTING)
            Bukkit.getScheduler().runTask(plugin) {
                data.copy(world = originalCreator.createWorld()).save(plugin)
                state = GameState.STOPPED
            }
        }
    }

    // SETTING UP

    fun addSpawner(spawner: SpawnerType, location: Location) {

    }

    fun addBed(team: Team, block: Block) {

    }

    // DURING GAME EVENTS

    fun bedBreak(team: Team, player: Player) {
        val teamOfPlayer = getTeamOfPlayer(player) ?: return
        broadcast(
            "${team.displayName.formatWithColors(team.color)}'s bed was broken by " +
                    "${player.displayName.formatWithColors(teamOfPlayer.color)}!"
        )
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