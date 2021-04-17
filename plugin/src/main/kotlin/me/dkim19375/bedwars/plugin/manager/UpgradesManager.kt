package me.dkim19375.bedwars.plugin.manager

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.enumclass.GameState
import me.dkim19375.bedwars.plugin.enumclass.Team
import me.dkim19375.bedwars.plugin.enumclass.TrapType
import me.dkim19375.bedwars.plugin.enumclass.formatWithColors
import me.dkim19375.bedwars.plugin.util.isArmor
import me.dkim19375.bedwars.plugin.util.isWeapon
import me.dkim19375.bedwars.plugin.util.sendTitle
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
class UpgradesManager(plugin: BedwarsPlugin, val game: BedwarsGame) {
    val sharpness = mutableSetOf<Team>()
    val protection = mutableMapOf<Team, Int>()
    val haste = mutableMapOf<Team, Int>()
    val healPool = mutableSetOf<Team>()
    val firstTrap = mutableMapOf<Team, TrapType>()
    val secondTrap = mutableMapOf<Team, TrapType>()
    val thirdTrap = mutableMapOf<Team, TrapType>()
    private val times = mutableMapOf<UUID, Long>()

    init {
        Bukkit.getScheduler().runTaskTimer(plugin, {
            if (game.state != GameState.STARTED) {
                return@runTaskTimer
            }
            val players = game.getPlayersInGame().map(Bukkit::getPlayer).filter(Objects::nonNull)
            for (player in players) {
                val team = game.getTeamOfPlayer(player)?: continue
                val haste = haste[team]
                if (haste != null) {
                    player.addPotionEffect(PotionEffect(PotionEffectType.FAST_DIGGING, 100, haste - 1))
                }
                if (!healPool.contains(team)) {
                    continue
                }
                game.data.beds.firstOrNull { d ->
                    team == d.team && d.location.distance(player.location) < 8
                } ?: continue
                player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 80, 1))
            }
        }, 60L, 60L)
    }

    private fun applyToPlayer(player: Player, trap: TrapType) {
        trap.removeEffects.forEach(player::removePotionEffect)
        trap.effects.forEach { (effect, amplifier) ->
            player.addPotionEffect(PotionEffect(effect, trap.duration, amplifier - 1))
        }
    }

    fun applyUpgrades(player: Player) {
        val team = game.getTeamOfPlayer(player) ?: return
        val sharpness = sharpness.contains(team)
        val protection = protection[team]
        if (!sharpness && protection == null) return
        for (item in player.inventory.contents) {
            if (item.type.isArmor() && protection != null) {
                item.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, protection)
            }
            if (item.type.isWeapon() && sharpness) {
                item.addEnchantment(Enchantment.DAMAGE_ALL, 1)
            }
        }
    }

    fun addTrap(team: Team, type: TrapType) {
        when (getLevel(team)) {
            0 -> firstTrap[team] = type
            1 -> secondTrap[team] = type
            2 -> thirdTrap[team] = type
        }
    }

    fun getLevel(team: Team): Int {
        if (!firstTrap.containsKey(team)) {
            return 0
        }
        if (!secondTrap.containsKey(team)) {
            return 1
        }
        if (!thirdTrap.containsKey(team)) {
            return 2
        }
        return 3
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