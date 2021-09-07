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

package me.dkim19375.bedwars.plugin.manager

import com.alessiodp.parties.api.interfaces.PartyPlayer
import dev.triumphteam.gui.builder.item.ItemBuilder
import me.dkim19375.bedwars.api.enumclass.GameState
import me.dkim19375.bedwars.api.enumclass.Result
import me.dkim19375.bedwars.api.enumclass.Team
import me.dkim19375.bedwars.api.event.game.BedwarsBedBrokenEvent
import me.dkim19375.bedwars.api.event.game.BedwarsGameStartCountDownEvent
import me.dkim19375.bedwars.api.event.game.BedwarsGameStartEvent
import me.dkim19375.bedwars.api.event.player.BedwarsPlayerEliminatedEvent
import me.dkim19375.bedwars.api.event.player.BedwarsPlayerJoinEvent
import me.dkim19375.bedwars.api.event.player.BedwarsPlayerQuitEvent
import me.dkim19375.bedwars.api.event.player.BedwarsTeamEliminatedEvent
import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.api.getAPI
import me.dkim19375.bedwars.plugin.config.MainConfigSettings
import me.dkim19375.bedwars.plugin.data.GameData
import me.dkim19375.bedwars.plugin.data.MainShopConfigItem
import me.dkim19375.bedwars.plugin.data.PlayerData
import me.dkim19375.bedwars.plugin.enumclass.ArmorType
import me.dkim19375.bedwars.plugin.util.*
import me.dkim19375.dkimbukkitcore.data.LocationWrapper
import me.dkim19375.dkimbukkitcore.function.formatAll
import me.dkim19375.dkimbukkitcore.function.logInfo
import me.dkim19375.dkimcore.extension.runCatchingOrNull
import org.apache.commons.io.FileUtils
import org.bukkit.*
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.lang.reflect.Field
import java.nio.file.Paths
import java.util.*

@Suppress("JoinDeclarationAndAssignment", "MemberVisibilityCanBePrivate")
class BedwarsGame(private val plugin: BedwarsPlugin, data: GameData) {
    var state = GameState.LOBBY
    var countdown = 10
    var time: Long = 0
    val tempPlayers = mutableSetOf<UUID>()
    val players = mutableMapOf<Team, MutableSet<UUID>>()
    val eliminated = mutableSetOf<UUID>()
    val playersInLobby = mutableSetOf<UUID>()
    var task: BukkitTask? = null
    val worldName: String = data.world.name
    val beds = mutableMapOf<Team, Boolean>()
    val npcManager = NPCManager(plugin, data)
    val upgradesManager = UpgradesManager(plugin, this)
    val spawnerManager = SpawnerManager(plugin, this)
    val placedBlocks = mutableMapOf<LocationWrapper, MainShopConfigItem?>()
    val beforeData = mutableMapOf<UUID, PlayerData>()
    val hologramManager = SpawnerHologramManager(plugin, this)
    val kills = mutableMapOf<UUID, Int>()
    val trackers = mutableMapOf<UUID, Team>()
    val spectators = mutableSetOf<UUID>()

    var data = data
        private set
        get() {
            return plugin.dataFileManager.getGameData(worldName) ?: throw IllegalStateException("Game data is null!")
        }

    init {
        npcManager.disableAI()
    }

    companion object {
        private val teleportField: Field =
            Class.forName("com.alessiodp.parties.common.configuration.data.ConfigParties")
                .getField("ADDITIONAL_TELEPORT_ENABLE")
        private val teleport: Boolean
            get() = teleportField.getBoolean(null)
    }

    private fun reset(start: Boolean = false) {
        checkAsync("game reset")
        countdown = 10
        time = 0
        tempPlayers.clear()
        players.clear()
        eliminated.clear()
        task?.cancel()
        task = null
        beds.clear()
        upgradesManager.reset()
        spawnerManager.reset()
        placedBlocks.clear()
        hologramManager.stop()
        kills.clear()
        trackers.clear()
        spectators.clear()


        if (!start) {
            playersInLobby.clear()
            beforeData.clear()
        }
        if (start) {
            spawnerManager.start()
            hologramManager.start()
        }
    }

    fun start(force: Boolean): Result {
        checkAsync("game start")
        val result = canStart(force)
        if (result != Result.SUCCESS) {
            return result
        }
        val event = BedwarsGameStartCountDownEvent(getAPI())
        Bukkit.getPluginManager().callEvent(event)
        if (event.isCancelled) {
            return Result.CANCELLED
        }
        state = GameState.STARTING
        reset(true)
        logInfo("Game ${data.world.name} is starting!")
        task?.cancel()
        task = object : BukkitRunnable() {
            override fun run() {
                if (task == null) {
                    state = GameState.LOBBY
                    cancel()
                    return
                }
                if (!force && playersInLobby.size < data.minPlayers) {
                    broadcast("${ChatColor.RED}Start cancelled - not enough players!")
                    state = GameState.LOBBY
                    cancel()
                    return
                }
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

    fun getElapsedTime(): Long {
        return Delay.fromTime(time).seconds
    }

    private fun setupAfterStart() {
        checkAsync("game setup after start")
        val configManager = plugin.mainConfigManager
        update()
        val event = BedwarsGameStartEvent(getAPI())
        Bukkit.getPluginManager().callEvent(event)
        if (event.isCancelled) {
            return
        }
        var i = 1
        val teams = data.teams.toList()
        for (uuid in playersInLobby.shuffled()) {
            val player = Bukkit.getPlayer(uuid) ?: continue
            val teamData = teams[i % teams.size]
            val team = teamData.team
            val format = configManager.get(MainConfigSettings.TAB_NAME)
            val replaceMap = mapOf<String, Any>(
                "team_color" to team.chatColor,
                "team_name" to team.displayName,
                "team_uppercase" to team.displayName.uppercase(),
                "team_first_letter" to team.displayName.uppercase().first(),
            )
            player.playerListName = format.formatAll(player).let {
                var new = it
                for ((key, value) in replaceMap) {
                    new = new.replace("%$key%", value.toString())
                }
                new
            }
            val set = players.getOrDefault(team, mutableSetOf())
            set.add(player.uniqueId)
            players[team] = set
            i++
        }
        for (bed in data.beds) {
            if (players.keys.contains(bed.team)) {
                continue
            }
            val block = bed.location.block
            block.type = Material.AIR
            block.state.update()
            block.world.getNearbyEntities(block.location, 6.0, 6.0, 6.0)
                .mapNotNull { it as? Item }
                .filter { it.itemStack.type.isBed() }
                .forEach(Item::remove)
        }
        for (teamData in teams) {
            beds[teamData.team] = true
        }
        for (entry in players.entries) {
            val team = entry.key
            val players = entry.value.getPlayers()
            val spawn = (data.teams.getTeam(team) ?: continue).spawn
            for (player in players) {
                player.teleportUpdated(spawn)
            }
        }
        state = GameState.STARTED
        time = System.currentTimeMillis()
        spawnerManager.start()
        for (entry in players.entries) {
            val team = entry.key
            val players = entry.value.getPlayers()
            for (player in players) {
                giveItems(player, null, team)
            }
        }
    }

    fun stop(winner: Player?, team: Team) {
        checkAsync("game stop")
        state = GameState.GAME_END
        for (player in getPlayersInGame()) {
            val playerTeam = getTeamOfPlayer(player) ?: continue
            plugin.mainDataFile.get().getStatistics(player).run {
                if (team == playerTeam) {
                    wins++
                } else {
                    losses++
                }
                plugin.dataFileManager.save = true
            }
        }
        logInfo("${team.chatColor}${winner?.displayName ?: team.displayName} has won BedWars!")
        val sorted = kills.toList()
            .sortedBy { (_, value) -> value }
            .map { Bukkit.getPlayer(it.first) to it.second }
            .filter { it.first != null }
            .reversed()
            .toMutableList()
        val firstKiller: Pair<Player, Int>? = sorted.firstOrNull()
        sorted.removeFirstOrNull()
        val secondKiller: Pair<Player, Int>? = sorted.firstOrNull()
        sorted.removeFirstOrNull()
        val thirdKiller: Pair<Player, Int>? = sorted.firstOrNull()
        for (player in getPlayersInGame().getPlayers()) {
            val isEliminated = eliminated.contains(player.uniqueId)
            if (!isEliminated) {
                player.inventory.clearAll()
                player.gameMode = GameMode.ADVENTURE
                player.allowFlight = true
                player.isFlying = true
                player.teleport(data.spec)
                giveGameOverItems(player)
                player.sendTitle(
                    title = if (getTeamOfPlayer(player) == team) {
                        "${ChatColor.GOLD}${ChatColor.BOLD}VICTORY!"
                    } else {
                        "${ChatColor.RED}${ChatColor.BOLD}GAME OVER!"
                    },
                    stay = 80
                )
            }
            val divider = "${ChatColor.GREEN}${ChatColor.STRIKETHROUGH}------------------------------------------------"
            player.sendMessage(divider)
            player.sendMessage(" ")
            player.sendCenteredMessage("${ChatColor.WHITE}${ChatColor.BOLD}Bed Wars")
            player.sendMessage(" ")
            player.sendCenteredMessage(
                "${team.chatColor}${team.displayName} ${ChatColor.GRAY}${
                    if (winner != null) {
                        " - ${ChatColor.WHITE}${winner.displayName}"
                    } else ""
                }"
            )
            player.sendMessage(" ")
            val sendMessage = msg@{ color: ChatColor, num: String, killer: Pair<Player, Int>? ->
                if (killer == null) {
                    return@msg
                }
                player.sendCenteredMessage("$color${ChatColor.BOLD}$num Killer ${ChatColor.GRAY}- ${ChatColor.WHITE}${killer.first.displayName} ${ChatColor.GRAY}- ${killer.second}")
            }
            sendMessage(ChatColor.YELLOW, "1st", firstKiller)
            sendMessage(ChatColor.GOLD, "2nd", secondKiller)
            sendMessage(ChatColor.RED, "3rd", thirdKiller)
            player.sendMessage(" ")
            player.sendMessage(divider)
        }
        Bukkit.getScheduler().runTaskLater(plugin, {
            if (state == GameState.GAME_END) {
                forceStop()
            }
        }, 200L)
    }

    fun forceStop(whenDone: () -> Unit = {}) {
        checkAsync("force stop")
        getPlayersInGame().plus(tempPlayers).plus(eliminated).toSet().getPlayers().forEach(this::revertPlayer)
        state = GameState.REGENERATING_WORLD
        reset()
        regenerateMap {
            state = GameState.LOBBY
            whenDone()
        }
    }

    fun isEditing() = plugin.dataFileManager.isEditing(data)

    fun canStart(force: Boolean = false): Result {
        update(force)
        if (state in listOf(GameState.STARTED, GameState.STARTING, GameState.GAME_END)) {
            return Result.GAME_RUNNING
        }
        if (!force && playersInLobby.size < data.minPlayers) {
            return Result.NOT_ENOUGH_PLAYERS
        }
        if (state == GameState.REGENERATING_WORLD) {
            return Result.REGENERATING_WORLD
        }
        return Result.SUCCESS
    }

    fun update(force: Boolean = false) {
        checkAsync("game update")
        if (state != GameState.STARTED) {
            if (state != GameState.STARTING || force) {
                return
            }
            if (playersInLobby.size >= data.minPlayers) {
                return
            }
            state = GameState.LOBBY
            task?.cancel()
            task = null
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
            forceStop()
            return
        }
        if (getPlayersInGame().size == 1) {
            val player = Bukkit.getPlayer(getPlayersInGame().first())
            val team = getTeamOfPlayer(player) ?: run {
                forceStop()
                return
            }
            stop(player, team)
        }
    }

    fun addPlayer(player: Player): Result {
        checkAsync("add player")
        if (state != GameState.LOBBY && state != GameState.STARTING) {
            if (state == GameState.STARTED) {
                return Result.GAME_RUNNING
            }
            return Result.GAME_STOPPED
        }
        if (playersInLobby.size >= data.maxPlayers) {
            return Result.TOO_MANY_PLAYERS
        }
        val event = BedwarsPlayerJoinEvent(player, getAPI())
        Bukkit.getPluginManager().callEvent(event)
        if (event.isCancelled) {
            return Result.CANCELLED
        }
        playersInLobby.add(player.uniqueId)
        broadcast("${player.displayName}${ChatColor.GREEN} has joined the game! ${playersInLobby.size}/${data.maxPlayers}")
        val lobby = plugin.dataFileManager.getLobby()
        val gameLobby = data.lobby.clone()
        beforeData[player.uniqueId] = PlayerData.createDataAndReset(
            player,
            lobby,
            gameLobby
        )
        plugin.scoreboardManager.getScoreboard(player, true) // activate
        if (playersInLobby.size >= data.minPlayers) {
            start(false).message
        }
        plugin.partiesAPI?.getPartyPlayer(player.uniqueId)?.let { partyPlayer ->
            if (!teleport) {
                return@let
            }
            if (!plugin.mainConfigManager.get(MainConfigSettings.PARTIES_TELEPORT_AUTO)) {
                return@let
            }
            val party = partyPlayer.partyId?.let { plugin.partiesAPI?.getParty(it) } ?: return@let
            if (party.leader != player.uniqueId) {
                return@let
            }
            for (otherPlayer in party.onlineMembers.map(PartyPlayer::getPlayerUUID)
                .filterNot(getPlayersInGame()::contains)
                .filterNot(player.uniqueId::equals)
                .toSet()
                .getPlayers()) {
                if (addPlayer(otherPlayer) != Result.SUCCESS) {
                    break
                }
            }
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
        if (state != GameState.STARTED) {
            return
        }
        for (uuid in players.values.getCombinedValues()) {
            val p = Bukkit.getPlayer(uuid) ?: continue
            p.sendMessage(text)
        }
    }

    fun playerKilled(player: Player, inventory: List<ItemStack>) {
        checkAsync("player killed")
        val team = getTeamOfPlayer(player) ?: return
        val hasBed = beds.getOrDefault(team, false)
        plugin.mainDataFile.get().getStatistics(player).run {
            deaths++
            if (!hasBed) {
                finalDeaths++
            }
            plugin.dataFileManager.save = true
        }
        player.killer?.let { killer ->
            plugin.mainDataFile.get().getStatistics(killer).run {
                kills++
                if (!hasBed) {
                    finalKills++
                }
                plugin.dataFileManager.save = true
            }
        }
        if (!hasBed) {
            player.playerListName = player.displayName
            player.inventory.clearAll()
            player.gameMode = GameMode.ADVENTURE
            player.allowFlight = true
            player.isFlying = true
            player.teleport(data.spec)
            player.sendTitle("${ChatColor.RED}${ChatColor.BOLD}GAME OVER!", stay = 80)
            giveGameOverItems(player)
            player.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 255, false, false), true)
            players.getOrDefault(team, mutableSetOf()).remove(player.uniqueId)
            if (players[team].isNullOrEmpty()) {
                players.remove(team)
            }
            tempPlayers.add(player.uniqueId)
            update()
            tempPlayers.remove(player.uniqueId)
            eliminated.add(player.uniqueId)
            val event = BedwarsPlayerEliminatedEvent(player, getAPI())
            Bukkit.getPluginManager().callEvent(event)
            if (players.getOrDefault(team, mutableSetOf()).isEmpty()) {
                val eliminatedEvent = BedwarsTeamEliminatedEvent(event, team)
                Bukkit.getPluginManager().callEvent(eliminatedEvent)
            }
            return
        }
        val teamData = data.teams.getTeam(team) ?: return
        player.inventory.clearAll()
        player.gameMode = GameMode.SPECTATOR
        player.teleportUpdated(data.spec)
        spectators.add(player.uniqueId)
        object : BukkitRunnable() {
            var countDown = 5
            override fun run() {
                if (!getPlayersInGame().contains(player.uniqueId)) {
                    spectators.remove(player.uniqueId)
                    return
                }
                if (countDown <= 0) {
                    player.teleportUpdated(teamData.spawn)
                    player.gameMode = GameMode.SURVIVAL
                    player.sendOtherTitle("${ChatColor.GREEN}Respawned!")
                    giveItems(player, inventory, team)
                    spectators.remove(player.uniqueId)
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
    }

    fun giveItems(player: Player, items: List<ItemStack?>?, team: Team) {
        val configManager = plugin.shopConfigManager
        var armorType: ArmorType = ArmorType.LEATHER
        var addPick = false
        var addAxe = false
        var addShears = false
        player.inventory.clearAll()
        items?.forEach { item ->
            item ?: return@forEach
            if (item.type.name.endsWith("PICKAXE")) {
                addPick = true
                return@forEach
            }
            if (item.type.name.endsWith("AXE")) {
                addAxe = true
                return@forEach
            }
            if (item.type == Material.SHEARS) {
                addShears = true
                return@forEach
            }
            val armor = ArmorType.fromMaterial(item.type) ?: return@forEach
            if (armor != ArmorType.LEATHER) {
                armorType = armor
            }
        }
        configManager.getItemFromMaterial(Material.WOOD_SWORD)?.item?.toItemStack(team.color)?.let {
            player.inventory.addItem(it)
        }
        player.inventory.setItem(
            17, ItemBuilder.from(Material.COMPASS)
                .name("${ChatColor.GREEN}Compass ${ChatColor.GRAY}(Right Click)")
                .addAllFlags()
                .build()
        )
        player.inventory.helmet = team.getColored(ItemStack(Material.LEATHER_HELMET))
        player.inventory.chestplate = team.getColored(ItemStack(Material.LEATHER_CHESTPLATE))
        player.inventory.leggings = team.getColored(ItemStack(armorType.leggings))
        player.inventory.boots = team.getColored(ItemStack(armorType.boots))
        if (addPick) {
            configManager.getItemFromMaterial(Material.WOOD_PICKAXE)?.item?.toItemStack(team.color)?.let {
                player.inventory.addItem(it)
            }
        }
        if (addAxe) {
            configManager.getItemFromMaterial(Material.WOOD_AXE)?.item?.toItemStack(team.color)?.let {
                player.inventory.addItem(it)
            }
        }
        if (addShears) {
            configManager.getItemFromMaterial(Material.SHEARS)?.item?.toItemStack(team.color)?.let {
                player.inventory.addItem(it)
            }
        }
        upgradesManager.applyUpgrades(player)
    }

    fun revertPlayer(player: Player) {
        checkAsync("player reverting")
        plugin.scoreboardManager.getScoreboard(player, false).deactivate()
        player.scoreboard = Bukkit.getScoreboardManager().mainScoreboard
        plugin.gameManager.hiddenPlayers[player.uniqueId]?.let { set ->
            for (hiddenPlayer in set.getPlayers()) {
                player.showPlayer(hiddenPlayer)
            }
        }
        plugin.gameManager.hiddenPlayers.remove(player.uniqueId)
        val data = beforeData[player.uniqueId] ?: return
        if (!setOf(GameMode.SPECTATOR, GameMode.CREATIVE).contains(data.gamemode)) {
            player.isFlying = false
        }
        data.apply(player, plugin)
    }

    private fun giveGameOverItems(player: Player) {
        player.inventory.setItem(
            0, ItemBuilder.from(Material.COMPASS)
                .name("${ChatColor.GREEN}${ChatColor.BOLD}Teleporter ${ChatColor.GRAY}(Click)")
                .lore("Click to teleport to players!")
                .addAllFlags()
                .build()
        )
        player.inventory.setItem(
            7, ItemBuilder.from(Material.PAPER)
                .name("${ChatColor.AQUA}${ChatColor.BOLD}Play Again ${ChatColor.GRAY}(Click)")
                .lore("Click to play another game!")
                .addAllFlags()
                .build()
        )
        player.inventory.setItem(
            8, ItemBuilder.from(Material.BED)
                .name("${ChatColor.RED}${ChatColor.BOLD}Return to Lobby ${ChatColor.GRAY}(Click)")
                .lore("Click to leave the lobby!")
                .addAllFlags()
                .build()
        )
    }

    fun leavePlayer(player: Player, update: Boolean = true) {
        checkAsync("player leave")
        if (state != GameState.GAME_END) {
            plugin.mainDataFile.get().getStatistics(player).losses++
            plugin.dataFileManager.save = true
        }
        revertPlayer(player)
        player.playerListName = player.displayName
        val event = BedwarsPlayerQuitEvent(player, getAPI())
        Bukkit.getPluginManager().callEvent(event)
        if (state == GameState.LOBBY || state == GameState.STARTING) {
            if (!playersInLobby.contains(player.uniqueId)) {
                return
            }
            playersInLobby.remove(player.uniqueId)
            broadcast("${player.displayName}${ChatColor.RED} has left the game! ${playersInLobby.size}/${data.maxPlayers}")
            if (playersInLobby.size < data.minPlayers) {
                task?.cancel()
                state = GameState.LOBBY
                broadcast("${ChatColor.RED}Cancelled - Not enough players to start!")
            }
            return
        }
        if (state == GameState.GAME_END || eliminated.contains(player.uniqueId)) {
            update()
            return
        }
        if (state != GameState.STARTED) {
            return
        }
        val team = getTeamOfPlayer(player) ?: return
        player.playerListName = player.displayName
        players.getOrDefault(team, mutableSetOf()).remove(player.uniqueId)
        if (players[team].isNullOrEmpty()) {
            players.remove(team)
        }
        broadcast("${team.chatColor}${player.displayName}${ChatColor.RED} has left the game!")
        if (update) {
            update()
        }
        return
    }

    fun getPlayersInGame(): Set<UUID> = when (state) {
        GameState.LOBBY, GameState.STARTING -> playersInLobby.toSet()
        GameState.STARTED -> players.values.getCombinedValues().toSet()
        GameState.GAME_END -> players.values.getCombinedValues().plus(tempPlayers).plus(eliminated).toSet()
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
        checkAsync("map saving")
        for (uuid in getPlayersInGame()) {
            val player = Bukkit.getPlayer(uuid) ?: continue
            leavePlayer(player)
        }
        data.world.players.forEach(Player::kickPlayerFromWorld)
        val folder = data.world.worldFolder
        val originalCreator = WorldCreator(data.world.name).copy(data.world)
        if (!Bukkit.unloadWorld(data.world, true)) {
            throw IllegalStateException("Could not unload world!")
        }
        Bukkit.getScheduler().runTaskLater(plugin, {
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
        }, 2L)
    }

    fun getPlayersInTeam(team: Team): Set<UUID> = players.getOrDefault(team, setOf())

    fun regenerateMap(whenDone: () -> Unit = {}) {
        checkAsync("map regenerating")
        val dir = Paths.get(plugin.dataFolder.absolutePath, "worlds", data.world.name).toFile()
        if (!dir.exists()) {
            // Something went wrong here
            throw IllegalStateException("The directory for the world: ${data.world.name} doesn't exist! (${dir.absolutePath})")
        }
        for (uuid in getPlayersInGame()) {
            val player = Bukkit.getPlayer(uuid) ?: continue
            leavePlayer(player)
        }
        data.world.players.forEach(Player::kickPlayerFromWorld)
        state = GameState.REGENERATING_WORLD
        val folder = data.world.worldFolder
        if (!plugin.isEnabled) {
            return
        }
        Bukkit.getScheduler().runTaskLater(plugin, {
            val originalCreator = runCatchingOrNull { WorldCreator(worldName).copy(data.world) } ?: return@runTaskLater
            if (!data.world.name.unloadWorld()) {
                throw IllegalStateException("Could not unload world ${data.world.name}!")
            }
            Bukkit.getScheduler().runTaskAsynchronously(plugin) {
                FileUtils.forceDelete(folder)
                FileUtils.copyDirectory(dir, folder)
                Bukkit.getScheduler().runTask(plugin) {
                    val result = originalCreator.loadWorld()
                    if (!result.first) {
                        throw IllegalStateException("Could not load world ${data.world.name}!")
                    }
                    val world = result.second ?: throw IllegalStateException("Could not load world ${data.world.name}!")
                    data.copy(tempWorld = world).save(plugin)
                    logInfo("${world.name} has finished regenerating!")
                    whenDone()
                }
            }
        }, 2L)
    }

    fun bedBreak(team: Team, player: Player?) {
        if (beds[team] == false) {
            return
        }
        val event = BedwarsBedBrokenEvent(getAPI(), player, data.beds.first { it.team == team })
        Bukkit.getPluginManager().callEvent(event)
        if (event.isCancelled) {
            return
        }
        beds[team] = false
        getPlayersInTeam(team).getPlayers().forEach { p ->
            p.sendOtherTitle("${ChatColor.RED}BED DESTROYED!", "You will no longer respawn!")
        }
        if (player == null) {
            broadcast(
                "${ChatColor.BOLD}BED DESTRUCTION > ${team.chatColor}${team.displayName}${ChatColor.GRAY}'s " +
                        "bed was broken!"
            )
            return
        }
        plugin.mainDataFile.get().getStatistics(player).bedsBroken++
        plugin.dataFileManager.save = true
        val teamOfPlayer = getTeamOfPlayer(player) ?: return
        broadcast(
            "${ChatColor.BOLD}BED DESTRUCTION > ${team.chatColor}${team.displayName}${ChatColor.GRAY}'s " +
                    "bed was broken by " +
                    "${teamOfPlayer.chatColor}${player.displayName}${ChatColor.GRAY}!"
        )
        update()
    }
}