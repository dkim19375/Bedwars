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

package me.dkim19375.bedwars.plugin.data

import me.dkim19375.bedwars.api.data.BedwarsStatisticsData
import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.dkimcore.extension.setDecimalPlaces

data class StatisticsData(
    @Transient
    private val plugin: BedwarsPlugin,
    var kills: Int = 0,
    var finalKills: Int = 0,
    var deaths: Int = 0,
    var finalDeaths: Int = 0,
    var wins: Int = 0,
    var losses: Int = 0,
    var bedsBroken: Int = 0,
) : BedwarsStatisticsData {
    override fun getKillCount(): Int = kills

    override fun setKillCount(amount: Int) {
        kills = amount
        plugin.dataFileManager.save = true
    }

    override fun getFinalKillCount(): Int = finalKills

    override fun setFinalKillCount(amount: Int) {
        finalKills = amount
        plugin.dataFileManager.save = true
    }

    override fun getDeathCount(): Int = deaths

    override fun setDeathCount(amount: Int) {
        deaths = amount
        plugin.dataFileManager.save = true
    }

    override fun getFinalDeathCount(): Int = finalDeaths

    override fun setFinalDeathCount(amount: Int) {
        finalDeaths = amount
        plugin.dataFileManager.save = true
    }

    override fun getWinCount(): Int = wins

    override fun setWinCount(amount: Int) {
        wins = amount
        plugin.dataFileManager.save = true
    }

    override fun getLossCount(): Int = losses

    override fun setLossCount(amount: Int) {
        losses = amount
        plugin.dataFileManager.save = true
    }

    override fun getBedsBrokenCount(): Int = bedsBroken

    override fun setBedsBrokenCount(amount: Int) {
        bedsBroken = amount
        plugin.dataFileManager.save = true
    }

    override fun getKillDeathRatio(): Double = kills.safeDivide(deaths)

    override fun getFinalKillDeathRatio(): Double = finalKills.safeDivide(finalDeaths)

    override fun getWinLossRatio(): Double = wins.safeDivide(losses)

    private fun Int.safeDivide(other: Int): Double {
        if (other == 0) {
            return 0.0
        }
        return (toDouble() / other.toDouble()).setDecimalPlaces(15)
    }

    override fun toString(): String {
        return "StatisticsData(kills=$kills, finalKills=$finalKills, deaths=$deaths, finalDeaths=$finalDeaths, wins=$wins, losses=$losses, bedsBroken=$bedsBroken)"
    }


}