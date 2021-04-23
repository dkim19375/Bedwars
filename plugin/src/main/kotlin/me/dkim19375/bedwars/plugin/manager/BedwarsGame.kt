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
import me.dkim19375.bedwars.plugin.util.getTeam
import me.dkim19375.bedwars.plugin.util.sendOtherTitle
import org.apache.commons.io.FileUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.WorldCreator
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.nio.file.Paths
import java.util.*

@Suppress("JoinDeclarationAndAssignment", "MemberVisibilityCanBePrivate")
class BedwarsGame(private val plugin: BedwarsPlugin, data: GameData) {
    var state = GameState.LOBBY
    var countdown = 10
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
        Bukkit.getScheduler().runTaskTimer(plugin, {
            for (player in getPlayersInGame().getPlayers()) {
                player.foodLevel = 20
            }
        }, 20L, 20L)
    }

    fun start(force: Boolean): Result {
        val result = canStart(force)
        if (result != Result.SUCCESS) {
            return result
        }
        state = GameState.STARTING
        countdown = 10
        println("Game ${data.world.name} is starting!")
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
        update()
        upgradesManager.resetTask()
        var i = 1
        val teams = data.teams.toList()
        for (uuid in playersInLobby.shuffled()) {
            val player = Bukkit.getPlayer(uuid) ?: continue
            val teamData = teams[i % teams.size]
            val team = teamData.team
            player.playerListName = player.name.formatWithColors(team.color)
            val set = players.getOrDefault(team, mutableSetOf())
            set.add(player.uniqueId)
            players[team] = set
            i++
        }
        for (teamData in teams) {
            beds[teamData.team] = true
        }
        for (entry in players.entries) {
            val team = entry.key
            val players = entry.value.getPlayers()
            val spawn = (data.teams.getTeam(team) ?: continue).spawn
            for (player in players) {
                player.teleport(spawn)
            }
        }
        state = GameState.STARTED
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

    fun forceStop(whenDone: Runnable? = null) {
        getPlayersInGame().getPlayers().forEach { p -> leavePlayer(p, false) }
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
        regenerateMap {
            state = GameState.LOBBY
            whenDone?.run()
        }
    }

    private fun revertBack() {
        for (uuid in getPlayersInGame().toSet()) {
            val player = Bukkit.getPlayer(uuid) ?: continue
            revertPlayer(player)
        }
    }

    fun isEditing() = plugin.dataFileManager.isEditing(data)

    fun canStart(force: Boolean): Result {
        update()
        if (isRunning()) {
            return Result.GAME_RUNNING
        }
        if (plugin.gameManager.isGameRunning(data.world)) {
            return Result.GAME_IN_WORLD
        }
        if (!force && playersInLobby.size < data.minPlayers) {
            return Result.NOT_ENOUGH_PLAYERS
        }
        if (state == GameState.REGENERATING_WORLD) {
            return Result.REGENERATING_WORLD
        }
        return Result.SUCCESS
    }

    fun isRunning() = state.running

    fun update() {
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
        if (getPlayersInGame().isEmpty()) {
            throw IllegalStateException("No players in game.. game is still active")
        }
        if (getPlayersInGame().size == 1) {
            val player = Bukkit.getPlayer(getPlayersInGame().first())
            stop(player, getTeamOfPlayer(player)!!)
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
        val lobby = plugin.dataFileManager.getLobby()
        beforeData[player.uniqueId] = PlayerData.getPlayerAndReset(
            player,
            if (plugin.config.getBoolean("use-main-lobby") && lobby != null) lobby else data.lobby
        )
        plugin.scoreboardManager.getScoreboard(player).activate()
        if (playersInLobby.size >= data.minPlayers) {
            println("BEDWARS GAME START STATUS: ${start(false).message}")
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
            val teamData = data.teams.getTeam(team) ?: return
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

    fun revertPlayer(player: Player) {
        plugin.scoreboardManager.getScoreboard(player).deactivate()
        val data = beforeData[player.uniqueId] ?: return
        data.apply(player)
    }

    fun leavePlayer(player: Player, update: Boolean = true) {
        revertPlayer(player)
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
            revertPlayer(player)
            player.playerListName = player.displayName
            broadcast("${player.displayName.formatWithColors(team.color)}${ChatColor.RED} has left the game!")
            if (update) {
                update()
            }
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
        for (uuid in getPlayersInGame()) {
            val player = Bukkit.getPlayer(uuid) ?: continue
            leavePlayer(player)
        }
        for (player in data.world.players) {
            player.teleport(Bukkit.getWorld("world").spawnLocation)
        }
        val folder = data.world.worldFolder
        val originalCreator = WorldCreator(data.world.name).copy(data.world)
        if (!Bukkit.unloadWorld(data.world, true)) {
            throw RuntimeException("Could not unload world!")
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin) {
            folder.delete()
            val path = Paths.get(plugin.dataFolder.absolutePath, "worlds", data.world.name)
            path.toFile().mkdirs()
            val file = Paths.get(path.toFile().absolutePath, data.world.name).toFile()
            if (file.exists()) {
                FileUtils.forceDelete(Paths.get(path.toFile().absolutePath, data.world.name).toFile())
            }
            FileUtils.copyDirectory(folder, path.toFile())
            Bukkit.getScheduler().runTask(plugin, originalCreator::createWorld)
        }
    }

    fun getPlayersInTeam(team: Team): Set<UUID> {
        return players.getOrDefault(team, setOf())
    }

    fun regenerateMap(whenDone: Runnable? = null) {
        if (state != GameState.STOPPED) {
            return
        }
        val dir = Paths.get(plugin.dataFolder.absolutePath, "worlds", data.world.name).toFile()
        if (!dir.exists()) {
            // Something went wrong here
            throw IllegalStateException("The directory for the world: ${data.world.name} doesn't exist! (${dir.absolutePath})")
        }
        for (uuid in getPlayersInGame()) {
            val player = Bukkit.getPlayer(uuid) ?: continue
            leavePlayer(player)
        }
        for (player in data.world.players) {
            player.teleport(Bukkit.getWorld("world").spawnLocation)
        }
        state = GameState.REGENERATING_WORLD
        val folder = data.world.worldFolder
        val originalCreator = WorldCreator(data.world.name).copy(data.world)
        if (!Bukkit.unloadWorld(data.world, true)) {
            throw RuntimeException("Could not unload world!")
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin) {
            FileUtils.forceDelete(folder)
            FileUtils.copyDirectory(dir, folder)
            Bukkit.getScheduler().runTask(plugin) {
                data.copy(world = originalCreator.createWorld()).save(plugin)
                whenDone?.run()
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
        update()
    }

    enum class Result(val message: String) {
        SUCCESS("Successful!"),
        GAME_RUNNING("The game is currently running!"),
        GAME_IN_WORLD("The game is in the same world!"),
        NOT_ENOUGH_PLAYERS("Not enough players!"),
        REGENERATING_WORLD("The game world is regenerating!"),
        TOO_MANY_PLAYERS("Too many players!")
    }
}