/*
 * MIT License
 *
 * Copyright (c) 2021 dkim19375
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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