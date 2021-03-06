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

import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion
import me.dkim19375.bedwars.api.enumclass.GameState
import me.dkim19375.bedwars.api.enumclass.Team
import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.NEW_MATERIALS
import me.dkim19375.bedwars.plugin.builder.GameBuilder
import me.dkim19375.bedwars.plugin.config.MainConfigSettings
import me.dkim19375.bedwars.plugin.data.GameData
import me.dkim19375.bedwars.plugin.util.getIgnoreCase
import me.dkim19375.bedwars.plugin.util.sendActionBar
import me.dkim19375.dkimbukkitcore.function.getPlayers
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Effect
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.potion.PotionEffectType
import java.util.*

private val FOOTSTEP_PARTICLES: Boolean = !MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_13_R1)

@Suppress("MemberVisibilityCanBePrivate")
class GameManager(private val plugin: BedwarsPlugin) {
    private val games = mutableMapOf<String, BedwarsGame>()
    val builders = mutableMapOf<String, GameBuilder>()
    val invisPlayers = mutableSetOf<UUID>()
    private val explosives = mutableMapOf<UUID, UUID>()
    val hiddenPlayers = mutableMapOf<UUID, MutableSet<UUID>>()

    init {
        Bukkit.getScheduler().runTaskTimer(plugin, {
            updateInvisPlayers()
            for (game in getGames().values) {
                updateSaturation(game)
                updateTrackers(game)
                updateTab(game)
            }
        }, 20L, 20L)
        if (FOOTSTEP_PARTICLES) {
            Bukkit.getScheduler().runTaskTimer(plugin, {
                // updateInvisParticles()
            }, 4L, 4L)
        }
        Bukkit.getScheduler().runTask(plugin, this::reloadData)
    }

    private fun updateInvisParticles() {
        if (NEW_MATERIALS) {
            return
        }
        for (player in /* invisPlayers.getPlayers() */ Bukkit.getOnlinePlayers()) {
            if (player.location.y <= 0) {
                continue
            }
            if (!player.location.block.getRelative(BlockFace.DOWN).type.isSolid) {
                continue
            }
            val newY = player.location.y % 1
            if (newY > 0.2) {
                continue
            }
            player.world.playEffect(player.location.add(0.0, 0.01, 0.0), Effect.FOOTSTEP, 0)
        }
    }

    private fun updateSaturation(game: BedwarsGame) {
        for (player in game.getPlayersInGame().getPlayers()) {
            player.foodLevel = 20
            player.saturation = 1f
        }
    }

    private fun updateTrackers(game: BedwarsGame) {
        if (game.state != GameState.STARTED) {
            return
        }
        for ((uuid, team) in game.trackers) {
            val player = Bukkit.getPlayer(uuid) ?: continue
            if (game.beds[team] != true) {
                continue
            }
            var nearestPlayer: Pair<Player, Double>? = null
            for (otherPlayer in game.getPlayersInTeam(team).getPlayers()) {
                val distance = player.location.distance(otherPlayer.location)
                if (nearestPlayer == null) {
                    nearestPlayer = otherPlayer to distance
                    continue
                }
                if (nearestPlayer.second > distance) {
                    nearestPlayer = otherPlayer to distance
                }
            }
            player.compassTarget = (nearestPlayer?.first?.location ?: player.world.spawnLocation).clone()
            player.sendActionBar(
                if (nearestPlayer == null) {
                    null
                } else {
                    "Tracking: ${team.chatColor}${nearestPlayer.first.name} ${ChatColor.WHITE}- " +
                            "Distance: ${ChatColor.GREEN}${ChatColor.BOLD}${nearestPlayer.second.toInt()}m"
                }
            )
        }
    }

    private fun updateTab(game: BedwarsGame) {
        if (game.state !in setOf(GameState.STARTED, GameState.STARTING, GameState.LOBBY, GameState.GAME_END)) {
            return
        }
        val manager = plugin.mainConfigManager
        val sameWorld = manager.get(MainConfigSettings.HIDE_SAME_WORLD)
        if (!manager.get(MainConfigSettings.HIDE_ENABLED)) {
            return
        }
        for (player in game.getPlayersInGame().getPlayers()) {
            for (onlinePlayer in Bukkit.getOnlinePlayers()) {
                if (game.getPlayersInGame().contains(onlinePlayer.uniqueId)
                    || (player.world.name == onlinePlayer.world.name && sameWorld)
                ) {
                    if (hiddenPlayers[player.uniqueId]?.contains(onlinePlayer.uniqueId) == true) {
                        player.showPlayer(onlinePlayer)
                        onlinePlayer.showPlayer(player)
                        hiddenPlayers[player.uniqueId]?.remove(onlinePlayer.uniqueId)
                    }
                    continue
                }
                player.hidePlayer(onlinePlayer)
                onlinePlayer.hidePlayer(player)
                hiddenPlayers.getOrPut(player.uniqueId) { mutableSetOf() }.add(onlinePlayer.uniqueId)
            }
        }
    }

    private fun updateInvisPlayers() {
        for (uuid in getAllPlayers().toSet()) {
            val player = Bukkit.getPlayer(uuid) ?: continue
            if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                invisPlayers.add(player.uniqueId)
                continue
            }
            invisPlayers.remove(player.uniqueId)
        }
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

    fun addExplosive(entityUUID: UUID, player: Player) = addExplosive(entityUUID, player.uniqueId)

    fun addExplosive(entityUUID: UUID, player: UUID) {
        explosives[entityUUID] = player
    }

    fun removeExplosive(entityUUID: UUID) = explosives.remove(entityUUID)

    fun getExplosives() = explosives.toMap()

    fun getGame(world: World): BedwarsGame? = getGame(world.name)

    fun getGame(player: UUID): BedwarsGame? {
        val gameName = getPlayerInGame(player) ?: return null
        return getGame(gameName)
    }

    fun getGame(player: Player): BedwarsGame? = getGame(player.uniqueId)

    fun getGame(name: String?, player: Player? = null): BedwarsGame? = name?.let { games.getIgnoreCase(it) }
        ?: player?.world?.name?.let(this::getGame)

    fun getGames(): Map<String, BedwarsGame> = games.toMap()

    fun addGame(game: BedwarsGame) {
        games[game.worldName] = game
    }

    fun deleteGame(game: BedwarsGame) = deleteGame(game.data)

    fun deleteGame(game: GameData) {
        games.remove(game.world.name)
        plugin.dataFileManager.setEditing(game, false)
        plugin.dataFileManager.removeGameData(game)
    }

    @Suppress("unused")
    fun getVillagers() = getShopVillagers().toMutableSet().plus(getUpgradeVillagers()).toSet()

    fun getUpgradeVillagers(): Set<Villager> {
        val set = mutableSetOf<Villager>()
        for (game in games.values) {
            set.addAll(game.npcManager.getUpgradeVillagers())
        }
        return set.toSet()
    }

    fun getShopVillagers(): Set<Villager> {
        val set = mutableSetOf<Villager>()
        for (game in games.values) {
            set.addAll(game.npcManager.getShopVillagers())
        }
        return set.toSet()
    }

    fun getVillagersUUID() = getShopVillagersUUID().toMutableSet().plus(getUpgradeVillagersUUID()).toSet()

    fun getUpgradeVillagersUUID(): Set<UUID> {
        val set = mutableSetOf<UUID>()
        for (game in games.values) {
            set.addAll(game.npcManager.getUpgradeVillagersUUID())
        }
        return set.toSet()
    }

    @Suppress("unused")
    fun removeVillager(villager: UUID) {
        for (game in games.values) {
            game.npcManager.removeVillager(villager)
        }
    }

    fun getShopVillagersUUID(): Set<UUID> {
        val set = mutableSetOf<UUID>()
        for (game in games.values) {
            set.addAll(game.npcManager.getShopVillagersUUID())
        }
        return set.toSet()
    }

    fun getRunningGames(): Map<String, BedwarsGame> {
        val map = mutableMapOf<String, BedwarsGame>()
        for (entry in games.entries) {
            if (setOf(GameState.STARTED, GameState.STARTING).contains(entry.value.state)) {
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
            if (game.getPlayersInGame().contains(player)) {
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