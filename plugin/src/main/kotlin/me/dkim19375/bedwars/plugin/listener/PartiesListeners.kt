package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.enumclass.GameState
import me.dkim19375.bedwars.plugin.manager.BedwarsGame
import me.dkim19375.dkim19375core.function.filterNonNull
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import java.util.*

class PartiesListeners(private val plugin: BedwarsPlugin) : Listener {
    private val api = plugin.partiesAPI
    private val teleports = mutableSetOf<UUID>()

    fun onGameJoin(player: Player, game: BedwarsGame): Boolean {
        api ?: println("API IS NULL!")
        val api = api ?: return true
        if (!teleports.contains(player.uniqueId)) {
            println("teleports: $teleports does not contain player: ${player.name}")
            return true
        }
        api.getPartyPlayer(player.uniqueId) ?: println("Party player is null")
        val partyPlayer = api.getPartyPlayer(player.uniqueId) ?: return true
        partyPlayer.partyId ?: println("party id is null")
        api.getParty(partyPlayer.partyId ?: return true) ?: println("party is null")
        val party = api.getParty(partyPlayer.partyId ?: return true) ?: return true
        val partyPlayers = party.onlineMembers.filter { p -> !game.getPlayersInGame().contains(p.playerUUID) }
        val players = partyPlayers.map { p -> Bukkit.getPlayer(p.playerUUID ) }.filterNonNull()
        if ((players.size + 1) > (game.data.maxPlayers - game.playersInLobby.size)) {
            player.sendMessage("The party is too large to join the game!")
            return false
        }
        players.forEach(game::addPlayer)
        println("added players")
        return true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private fun PlayerCommandPreprocessEvent.onCommand() {
        val api = api ?: return
        val command = message.replace("/", "").trim()
        val game = plugin.gameManager.getGame(player) ?: return
        if (!command.equals("party teleport", ignoreCase = true)) {
            return
        }
        if (!player.hasPermission("parties.user.teleport")) {
            return
        }
        val partyPlayer = api.getPartyPlayer(player.uniqueId) ?: return
        if (!partyPlayer.isInParty) {
            return
        }
        val partyId = partyPlayer.partyId ?: return
        val party = api.getParty(partyId) ?: return
        if (game.state != GameState.LOBBY && game.state != GameState.STARTING) {
            player.sendMessage("You must be in the lobby!")
            isCancelled = true
            return
        }
        if ((party.onlineMembers.size - 1) > (game.data.maxPlayers - game.playersInLobby.size)) {
            isCancelled = true
            player.sendMessage("The party is too large to join the game!")
            return
        }
        teleports.add(player.uniqueId)
    }
}