package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.enumclass.GameState
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

    fun onGameJoin(player: Player): Boolean {
        val api = api ?: return true
        if (!teleports.contains(player.uniqueId)) {
            return true
        }
        val partyPlayer = api.getPartyPlayer(player.uniqueId) ?: return true
        val party = api.getParty(partyPlayer.partyId ?: return true) ?: return true
        val partyPlayers = party.onlineMembers.filter { p -> p.playerUUID != partyPlayer.playerUUID }
        val players = partyPlayers.map { p -> Bukkit.getPlayer(p.playerUUID ) }.filterNonNull()
        val game = plugin.gameManager.getGame(player) ?: return true
        if (game.state != GameState.LOBBY) {
            return true
        }
        if ((players.size + 1) > (game.data.maxPlayers - game.playersInLobby.size)) {
            player.sendMessage("The party is too large to join the game!")
            return false
        }
        players.forEach(game::addPlayer)
        return true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private fun PlayerCommandPreprocessEvent.onCommand() {
        val api = api ?: return
        val command = message.replace("/", "").trim()
        val game = plugin.gameManager.getGame(player) ?: return
        if (game.state != GameState.LOBBY) {
            return
        }
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
        if ((party.onlineMembers.size - 1) > (game.data.maxPlayers - game.playersInLobby.size)) {
            isCancelled = true
            player.sendMessage("The party is too large to join the game!")
            return
        }
        teleports.add(player.uniqueId)
    }
}