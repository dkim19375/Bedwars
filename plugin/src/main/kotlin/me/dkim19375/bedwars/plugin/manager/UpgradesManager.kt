package me.dkim19375.bedwars.plugin.manager

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.enumclass.Team
import me.dkim19375.bedwars.plugin.enumclass.TrapType
import me.dkim19375.bedwars.plugin.enumclass.formatWithColors
import me.dkim19375.bedwars.plugin.util.sendTitle
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import java.util.*

class UpgradesManager(private val plugin: BedwarsPlugin, val game: BedwarsGame) {
    val sharpness = mutableMapOf<Team, Int>()
    val protection = mutableMapOf<Team, Int>()
    val haste = mutableMapOf<Team, Int>()
    val healPool = mutableSetOf<Team>()
    val firstTrap = mutableMapOf<Team, TrapType>()
    val secondTrap = mutableMapOf<Team, TrapType>()
    val thirdTrap = mutableMapOf<Team, TrapType>()
    private val times = mutableMapOf<UUID, Long>()

    private fun applyToPlayer(player: Player, trap: TrapType) {
        trap.removeEffects.forEach(player::removePotionEffect)
        trap.effects.forEach { (effect, amplifier) ->
            player.addPotionEffect(PotionEffect(effect, trap.duration, amplifier - 1))
        }
    }

    fun applyUpgrades(player: Player) {

    }

    fun triggerTrap(player: Player) {
        val teamOfPlayer = game.getTeamOfPlayer(player) ?: return
        val bedData = game.data.beds.firstOrNull { d ->
            teamOfPlayer != d.team
            d.location.distance(player.location) < 8
        } ?: return
        val team = bedData.team
        val trap = firstTrap[team] ?: return
        firstTrap.remove(team)
        val secondTrap = secondTrap[team]
        val thirdTrap = thirdTrap[team]
        if (secondTrap != null) {
            firstTrap[team] = secondTrap
        }
        if (thirdTrap != null) {
            this.secondTrap[team] = thirdTrap
        }

        // on trigger
        applyToPlayer(player, trap)
        for (uuid in game.getPlayersInTeam(team)) {
            val p = Bukkit.getPlayer(uuid) ?: continue
            p.sendTitle("${ChatColor.RED}Your trap has been triggered!")
            p.sendMessage(
                "${ChatColor.RED}${ChatColor.BOLD}${trap.displayName} trap set off${
                    if (trap == TrapType.ALARM) " by ${player.name.formatWithColors(teamOfPlayer.color)}${ChatColor.RED}${ChatColor.BOLD}"
                    else ""
                }!"
            )
        }
    }

    fun canAlertTrap(player: Player): Boolean {
        val teamOfPlayer = game.getTeamOfPlayer(player) ?: return false
        game.data.beds.firstOrNull { d ->
            teamOfPlayer != d.team
            d.location.distance(player.location) < 8
        } ?: return false
        val time = times[player.uniqueId] ?: return true
        if (System.currentTimeMillis() - time > 30000) {
            times.remove(player.uniqueId)
            return true
        }
        return false
    }
}