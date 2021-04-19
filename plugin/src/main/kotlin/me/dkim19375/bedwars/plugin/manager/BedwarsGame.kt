package me.dkim19375.bedwars.plugin.manager

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.data.GameData
import me.dkim19375.bedwars.plugin.data.LocationWrapper
import me.dkim19375.bedwars.plugin.data.PlayerData
import me.dkim19375.bedwars.plugin.enumclass.GameState
import me.dkim19375.bedwars.plugin.enumclass.Team
import me.dkim19375.bedwars.plugin.enumclass.formatWithColors
import me.dkim19375.bedwars.plugin.util.getCombinedValues
import me.dkim19375.bedwars.plugin.util.getPlayers
import me.dkim19375.bedwars.plugin.util.sendOtherTitle
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.WorldCreator
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
    val beforeData = mutableMapOf<UUID, PlayerData>()

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
            val player = Bukkit.getPlayer(uuid) ?: continue
            val teamData = teams[i % teams.size]
            val team = teamData.first
            player.playerListName = player.name.formatWithColors(team.color)
            val set = players.getOrDefault(team, mutableSetOf())
            set.add(player.uniqueId)
            players[team] = set
            i++
        }
        for (team in teams) {
            beds[team.first] = true
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
        getPlayersInGame().getPlayers().forEach(this::leavePlayer)
        players.clear()
        playersInLobby.clear()
        task?.cancel()
        task = null
        beds.clear()
        time = 0
        state = GameState.STOPPED
        spawnerManager.reset()
        upgradesManager.stop()
        placedBlocks.clear()
        revertBack()
        beforeData.clear()
        regenerateMap()
        state = GameState.LOBBY
    }

    private fun revertBack() {
        for (uuid in getPlayersInGame().toSet()) {
            revertPlayer(uuid)
        }
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
        if (state != GameState.STARTED) {
            return
        }
        for (team in players.keys.toList()) {
            for (player in players.getOrDefault(team, setOf()).toList()) {
                if (Bukkit.getPlayer(player) != null) continue
                players.getOrDefault(team, mutableSetOf()).remove(player)
            }
            if (beds[team] == true && players.getOrDefault(team, setOf()).isEmpty()) {
                bedBreak(team, null)
            }
        }
    }

    fun addPlayer(player: Player): Result {
        if (state != GameState.LOBBY) {
            if (isRunning()) {
                return Result.GAME_RUNNING
            }
        }
        if (playersInLobby.size >= data.maxPlayers) {
            return Result.TOO_MANY_PLAYERS
        }
        playersInLobby.add(player.uniqueId)
        broadcast("${player.displayName}${ChatColor.GREEN} has joined the game! ${playersInLobby.size}/${data.maxPlayers}")
        player.gameMode = GameMode.CREATIVE
        val lobby = plugin.dataFileManager.getLobby()
        beforeData[player.uniqueId] = PlayerData.getPlayerAndReset(
            player,
            if (plugin.config.getBoolean("use-main-lobby") && lobby != null) lobby else data.lobby
        )
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
        val team = getTeamOfPlayer(player) ?: return
        if (beds.getOrDefault(team, false)) {
            val teamData = data.teams[team] ?: return
            player.inventory.clear()
            player.gameMode = GameMode.SPECTATOR
            player.teleport(data.spec)
            object : BukkitRunnable() {
                var countDown = 5
                override fun run() {
                    if (countDown <= 0) {
                        player.teleport(teamData.spawn)
                        player.gameMode = GameMode.SURVIVAL
                        player.sendOtherTitle("${ChatColor.GREEN}Respawned!")
                        cancel()
                        return
                    }
                    player.sendOtherTitle(
                        "${ChatColor.RED}You died!",
                        "${ChatColor.YELLOW}Respawning in ${ChatColor.GREEN}$countDown",
                        fadeIn = 0,
                        stay = 25,
                        fadeOut = 0
                    )
                    countDown--
                }
            }.runTaskTimer(plugin, 0L, 20L)
            return
        }
        leavePlayer(player)
    }

    fun revertPlayer(uuid: UUID) {
        val player = Bukkit.getPlayer(uuid) ?: return
        val data = beforeData[uuid] ?: return
        data.apply(player)
    }

    fun leavePlayer(player: Player) {
        revertPlayer(player.uniqueId)
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
            val team = getTeamOfPlayer(player) ?: return
            revertPlayer(player.uniqueId)
            player.playerListName = player.displayName
            broadcast("${player.displayName.formatWithColors(team.color)}${ChatColor.RED} has left the game!")
            updatePlayers()
            return
        }
    }

    fun getPlayersInGame(): Set<UUID> = when (state) {
        GameState.LOBBY -> playersInLobby.toSet()
        GameState.STARTED -> players.values.getCombinedValues().toSet()
        else -> setOf()
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

    // DURING GAME EVENTS

    fun bedBreak(team: Team, player: Player?) {
        if (beds[team] == false) {
            return
        }
        beds[team] = false
        getPlayersInTeam(team).getPlayers().forEach { p ->
            p.sendOtherTitle("${ChatColor.RED}BED DESTROYED!", "You will no longer respawn!")
        }
        if (player == null) {
            broadcast(
                "${ChatColor.BOLD}BED DESTRUCTION > ${team.displayName.formatWithColors(team.color)}${ChatColor.GRAY}'s " +
                        "bed was broken!"
            )
            return
        }
        val teamOfPlayer = getTeamOfPlayer(player) ?: return
        broadcast(
            "${ChatColor.BOLD}BED DESTRUCTION > ${team.displayName.formatWithColors(team.color)}${ChatColor.GRAY}'s " +
                    "bed was broken by " +
                    "${player.displayName.formatWithColors(teamOfPlayer.color)}${ChatColor.GRAY}!"
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