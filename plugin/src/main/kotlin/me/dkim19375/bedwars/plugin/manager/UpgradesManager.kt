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

import dev.triumphteam.gui.builder.item.ItemBuilder
import me.dkim19375.bedwars.api.enumclass.GameState
import me.dkim19375.bedwars.api.enumclass.Team
import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.enumclass.TrapType
import me.dkim19375.bedwars.plugin.util.*
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
class UpgradesManager(private val plugin: BedwarsPlugin, private val game: BedwarsGame) {
    val sharpness = mutableSetOf<Team>()
    val protection = mutableMapOf<Team, Int>()
    val haste = mutableMapOf<Team, Int>()
    val healPool = mutableSetOf<Team>()
    val firstTrap = mutableMapOf<Team, TrapType>()
    val secondTrap = mutableMapOf<Team, TrapType>()
    val thirdTrap = mutableMapOf<Team, TrapType>()
    private val times = mutableMapOf<UUID, Long>()

    fun reset() {
        sharpness.clear()
        protection.clear()
        haste.clear()
        healPool.clear()
        firstTrap.clear()
        secondTrap.clear()
        thirdTrap.clear()
        times.clear()
    }

    init {
        Bukkit.getScheduler().runTaskTimer(plugin, {
            if (game.state != GameState.STARTED) {
                return@runTaskTimer
            }
            val players = game.getPlayersInGame().getPlayers()
            for (player in players) {
                applyHaste(player)
                applyHealPool(player)
            }
        }, 50L, 50L)
    }

    private fun applyHaste(player: Player) {
        val team = game.getTeamOfPlayer(player) ?: return
        val haste = haste[team] ?: return
        player.addPotionEffect(PotionEffect(PotionEffectType.FAST_DIGGING, 120, haste - 1))
    }

    private fun applyHealPool(player: Player) {
        val team = game.getTeamOfPlayer(player) ?: return
        if (!healPool.contains(team)) {
            return
        }
        game.data.beds.firstOrNull { d ->
            team == d.team && d.location.getSafeDistance(player.location) < 7
        } ?: return
        player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 120, 0))
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
        for (item in player.inventory.getAllContents().toList().filterNotNull()) {
            if (item.type.isArmor() && protection != null) {
                item.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, protection)
            }
            if (item.type.isWeapon() && sharpness) {
                item.addEnchantment(Enchantment.DAMAGE_ALL, 1)
            }
        }
    }

    fun applyToItem(item: ItemBuilder, player: Player) {
        val team = game.getTeamOfPlayer(player) ?: return
        val sharpness = sharpness.contains(team)
        val protection = protection[team]
        if (item.build().type.isArmor() && protection != null) {
            item.enchant(Enchantment.PROTECTION_ENVIRONMENTAL, protection, true)
        }
        if (item.build().type.isWeapon() && sharpness) {
            item.enchant(Enchantment.DAMAGE_ALL, 1, true)
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
        if (!canAlertTrap(player)) {
            return
        }
        val teamOfPlayer = game.getTeamOfPlayer(player) ?: return
        val bedData = game.data.beds.firstOrNull { d ->
            teamOfPlayer != d.team
            d.location.getSafeDistance(player.location) < 8
        } ?: return
        val team = bedData.team
        val trap = firstTrap[team] ?: return
        firstTrap.remove(team)
        val secondTrap = secondTrap[team]
        val thirdTrap = thirdTrap[team]
        if (secondTrap != null) {
            firstTrap[team] = secondTrap
            this.secondTrap.remove(team)
        }
        if (thirdTrap != null) {
            this.secondTrap[team] = thirdTrap
            this.thirdTrap.remove(team)
        }

        // on trigger
        times[player.uniqueId] = System.currentTimeMillis()
        applyToPlayer(player, trap)
        for (uuid in game.getPlayersInTeam(team)) {
            val p = Bukkit.getPlayer(uuid) ?: continue
            p.sendTitle("${ChatColor.RED}Your trap has been triggered!")
            p.sendMessage(
                "${ChatColor.RED}${ChatColor.BOLD}${trap.displayName} trap set off${
                    if (trap == TrapType.ALARM) " by ${teamOfPlayer.chatColor}${player.name}${ChatColor.RED}${ChatColor.BOLD}"
                    else ""
                }!"
            )
        }
    }

    fun canAlertTrap(player: Player): Boolean {
        val teamOfPlayer = game.getTeamOfPlayer(player) ?: return false
        val trapConfig = plugin.config.getConfigurationSection("trap")
        val range = trapConfig?.getIntOrNull("range") ?: 7
        val cooldown = (trapConfig?.getIntOrNull("cooldown") ?: 20) * 1000
        game.data.beds.firstOrNull { d ->
            (teamOfPlayer != d.team) &&
                    (d.location.getSafeDistance(player.location) < range) &&
                    (firstTrap.containsKey(d.team))
        } ?: return false
        val time = times[player.uniqueId]
        if (time == null || System.currentTimeMillis() - time > cooldown) {
            times.remove(player.uniqueId)
            return true
        }
        return false
    }
}