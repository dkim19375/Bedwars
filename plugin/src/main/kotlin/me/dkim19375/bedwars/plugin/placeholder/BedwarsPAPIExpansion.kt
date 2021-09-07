package me.dkim19375.bedwars.plugin.placeholder

import me.clip.placeholderapi.PlaceholderAPI
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.dkim19375.bedwars.api.enumclass.Team
import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.data.TeamData
import me.dkim19375.bedwars.plugin.manager.GameManager
import me.dkim19375.bedwars.plugin.util.containsIgnoreCase
import me.dkim19375.bedwars.plugin.util.formatTime
import me.dkim19375.bedwars.plugin.util.getCombinedValues
import me.dkim19375.bedwars.plugin.util.toRomanNumeral
import me.dkim19375.dkimbukkitcore.function.getPlayers
import me.dkim19375.dkimcore.extension.setDecimalPlaces
import me.dkim19375.dkimcore.extension.toUUID
import me.dkim19375.dkimcore.extension.typedNull
import org.apache.commons.lang.StringUtils
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

private val PLAYER_ARGS = setOf("kills", "deaths", "kd", "fkd", "wins", "losses", "wl", "bedsBroken", "game", "team")

class BedwarsPAPIExpansion(private val plugin: BedwarsPlugin) : PlaceholderExpansion() {

    private val gameManager: GameManager
        get() = plugin.gameManager

    override fun persist(): Boolean = true

    override fun canRegister(): Boolean = true

    override fun getAuthor(): String = plugin.description.authors.joinToString()

    override fun getIdentifier(): String = "bedwars"

    override fun getVersion(): String = plugin.description.version

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        if (params.isBlank()) {
            return null
        }
        val args = PlaceholderAPI.setBracketPlaceholders(player, params)
            .replace("{", "")
            .replace("}", "")
            .split('_')
        if (args.size < 2) {
            return null
        }
        if (player == null && !args[0].equals("player", true)) {
            if (params.equals("arenas_amount", true)) {
                return gameManager.getGames().size.toString()
            }
            return null
        }
        @Suppress("SpellCheckingInspection")
        when (args[0].lowercase()) {
            "arena" -> {
                if (args.size < 3) {
                    return null
                }
                val game = gameManager.getGame(args[1]) ?: return null
                when (args[2].lowercase()) {
                    "status" -> return if (args.getOrNull(3)?.equals("colored", true) == true) {
                        "${game.state.color}${game.state.displayname}"
                    } else {
                        game.state.displayname
                    }
                    "count" -> {
                        if (args.size < 4) {
                            return null
                        }
                        val activePlayers = game.players.values.getCombinedValues().toSet()
                        val allPlayers = game.getPlayersInGame()
                        return when (args[3].lowercase()) {
                            "activeplayers" -> activePlayers.getPlayers().size.toString()
                            "nonactiveplayers" -> allPlayers.minus(activePlayers).getPlayers().size.toString()
                            "totalplayers" -> allPlayers.size.toString()
                            "activeteams" -> game.players.size.toString()
                            "nonactiveteams" -> (game.data.teams.size - game.players.size).toString()
                            "totalteams" -> game.data.teams.size.toString()
                            else -> null
                        }
                    }
                    "time" -> return if (args.getOrNull(3)?.equals("formatted", true) == true) {
                        game.getElapsedTime().formatTime()
                    } else {
                        game.getElapsedTime().toString()
                    }
                    "upgrades" -> {
                        if (args.size < 4) {
                            return null
                        }
                        val closest = game.spawnerManager.getClosest() ?: return ""
                        val spawner = closest.first
                        val delay = closest.second
                        val tier = (game.spawnerManager.upgradeLevels.getOrDefault(spawner, 1) + 1).toRomanNumeral()
                        return when (args[3].lowercase()) {
                            "name" -> StringUtils.capitalize(closest.first.name.lowercase())
                            "tier" -> tier
                            "time" -> delay.formatTime()
                            else -> null
                        }
                    }
                    "world" -> return game.worldName
                    "minplayers" -> return game.data.minPlayers.toString()
                    "maxplayers" -> return game.data.maxPlayers.toString()
                    "team" -> {
                        if (args.size < 5) {
                            return null
                        }
                        val team = Team.fromString(args[3]) ?: return null
                        if (game.data.teams.map(TeamData::team).none(team::equals)) {
                            return null
                        }
                        return when (args[4].lowercase()) {
                            "name" -> if (args.getOrNull(5)?.equals("uppercase") == true) {
                                team.displayName.uppercase()
                            } else {
                                team.displayName
                            }
                            "players" -> game.getPlayersInTeam(team).size.toString()
                            else -> null
                        }
                    }
                    else -> return null
                }
            }
            "player" -> {
                val playerArg = args.getOrNull(2)?.let(PLAYER_ARGS::containsIgnoreCase) == true
                val nullReturn = if (params.endsWith("game", true) || params.endsWith("team", true)) {
                    "None"
                } else {
                    "0"
                }
                val statsPlayer = if (playerArg) {
                    plugin.mainDataFile.get().nameCache.toList().firstOrNull { (name, uuid) ->
                        name.equals(args[1], true) || name.toUUID()?.let(uuid::equals) == true
                    }?.second
                } else {
                    if (player == null) {
                        return nullReturn
                    }
                    player.uniqueId
                } ?: return nullReturn
                val stats = plugin.mainDataFile.get().getStatistics(statsPlayer)
                val final = args.getOrNull(if (playerArg) 3 else 2)?.equals("final", true) == true
                return (when (args[if (playerArg) 2 else 1].lowercase()) {
                    "kills" -> if (final) stats.finalKills else stats.kills
                    "deaths" -> if (final) stats.finalDeaths else stats.deaths
                    "kd" -> if (final) stats.finalKillDeathRatio else stats.killDeathRatio
                    "wins" -> stats.wins
                    "losses" -> stats.losses
                    "wl" -> stats.winLossRatio
                    "bedsbroken" -> stats.bedsBroken
                    "game" -> gameManager.getGame(statsPlayer)?.worldName ?: nullReturn
                    "team" -> Bukkit.getPlayer(statsPlayer)?.let(gameManager::getTeamOfPlayer)
                        ?.let(Team::getDisplayName) ?: nullReturn
                    else -> typedNull<Number>()
                })?.let { any: Any? ->
                    any ?: return@let null
                    val num = any as? Number ?: return any.toString()
                    if (num.toDouble().toInt() == num.toInt()) {
                        return@let num.toInt().toString()
                    }
                    return@let num.toDouble().setDecimalPlaces(2).toString()
                }
            }
            else -> return null
        }
    }
}