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

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.data.GameData
import me.dkim19375.bedwars.plugin.data.MainShopConfigItem
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class DataFileManager(private val plugin: BedwarsPlugin) {
    private var save = false

    init {
        Bukkit.getScheduler().runTask(plugin) {
            Bukkit.getScheduler().runTaskTimer(plugin, {
                if (save) {
                    plugin.dataFile.save()
                    save = false
                }
            }, 100L, 100L)
        }
    }

    fun getQuickBuySlot(slot: Int, player: UUID): MainShopConfigItem? {
        return JavaPlugin.getPlugin(BedwarsPlugin::class.java).configManager.getItemFromName(
            plugin.dataFile.config.getString(
                "$player.shop.quick-buy.$slot"
            )
        )
    }

    fun setQuickBuySlot(slot: Int, player: UUID, item: MainShopConfigItem?) {
        if (item == null) {
            plugin.dataFile.config.set("$player.shop.quick-buy.$slot", null)
            save = true
            return
        }
        plugin.dataFile.config.set("$player.shop.quick-buy.$slot", item.name)
        save = true
    }

    fun setGameData(data: GameData) {
        plugin.dataFile.config.set("game-data.${data.world.name}", data)
        save = true
    }

    fun removeGameData(data: GameData) {
        plugin.dataFile.config.set("game-data.${data.world.name}", null)
        save = true
    }

    fun getGameData(world: String): GameData? {
        return plugin.dataFile.config.get("game-data.$world") as? GameData
    }

    fun getGameDatas(): Set<GameData> {
        val section = plugin.dataFile.config.getConfigurationSection("game-data") ?: return emptySet()
        val dataSet = mutableSetOf<GameData>()
        for (key in section.getKeys(false)) {
            if (Bukkit.getWorld(key) == null) {
                continue
            }
            val data = section.get(key) ?: continue
            if (data !is GameData) {
                continue
            }
            dataSet.add(data)
        }
        return dataSet.toSet()
    }

    @Suppress("unused")
    fun setLobby(location: Location) {
        plugin.dataFile.config.set("lobby", location)
        save = true
    }

    fun getLobby(): Location? {
        return plugin.dataFile.config.get("lobby") as? Location
    }

    fun isEditing(data: GameData) = plugin.dataFile.config.getBoolean("editing.${data.world.name}")

    fun setEditing(data: GameData, editing: Boolean) {
        if (!editing) {
            plugin.dataFile.config.set("editing.${data.world.name}", null)
            save = true
            return
        }
        plugin.dataFile.config.set("editing.${data.world.name}", editing)
        save = true
        return
    }
}