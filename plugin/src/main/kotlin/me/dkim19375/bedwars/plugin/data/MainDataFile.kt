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

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

private val plugin: BedwarsPlugin = JavaPlugin.getPlugin(BedwarsPlugin::class.java)

data class MainDataFile(
    var quickBuySlots: MutableMap<UUID, MutableMap<Int, MainShopConfigItem>> = mutableMapOf(),
    var lobby: Location? = null,
    var editing: MutableSet<String> = mutableSetOf(),
    var statistics: MutableMap<UUID, StatisticsData> = mutableMapOf(),
    var nameCache: MutableMap<String, UUID> = mutableMapOf()
) {
    fun getStatistics(uuid: UUID): StatisticsData {
        Bukkit.getPlayer(uuid)?.let { player ->
            return getStatistics(player)
        }
        Bukkit.getOfflinePlayer(uuid)?.name?.let {
            nameCache[it] = uuid
        }
        return statistics.getOrPut(uuid) { StatisticsData(plugin) }
    }

    fun getStatistics(player: Player): StatisticsData {
        nameCache[player.name] = player.uniqueId
        return statistics.getOrPut(player.uniqueId) { StatisticsData(plugin) }
    }

    fun save() {
        plugin.mainDataFile.set(this)
        plugin.mainDataFile.save()
    }
}