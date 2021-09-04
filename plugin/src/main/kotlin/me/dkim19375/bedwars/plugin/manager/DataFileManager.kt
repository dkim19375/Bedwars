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
import me.dkim19375.bedwars.plugin.data.MainDataFile
import me.dkim19375.bedwars.plugin.data.MainShopConfigItem
import me.dkim19375.dkimcore.file.JsonFile
import org.bukkit.Bukkit
import org.bukkit.Location
import java.io.File
import java.util.*

class DataFileManager(private val plugin: BedwarsPlugin) {
    var save = false
    private val data: MainDataFile
        get() = plugin.mainDataFile.get()

    init {
        Bukkit.getScheduler().runTask(plugin) {
            Bukkit.getScheduler().runTaskTimer(plugin, {
                if (save) {
                    data.save()
                    for (data in plugin.gameDataFiles.values) {
                        data.save()
                    }
                    save = false
                }
            }, 100L, 100L)
        }
    }

    fun getQuickBuySlot(slot: Int, player: UUID): MainShopConfigItem? = data.quickBuySlots[player]?.get(slot)

    fun setQuickBuySlot(slot: Int, player: UUID, item: MainShopConfigItem?) {
        if (item == null) {
            data.quickBuySlots.getOrPut(player) { mutableMapOf() }.remove(slot)
            save = true
            return
        }
        data.quickBuySlots.getOrPut(player) { mutableMapOf() }[slot] = item
        save = true
    }

    fun setGameData(data: GameData) {
        removeGameData(data)
        val newData = JsonFile(
            type = GameData::class,
            fileName = File(plugin.dataFolder, "data/games/${data.world.name}.json").path,
            prettyPrinting = true,
            typeAdapters = plugin.jsonSerializers,
            default = { data }
        )
        plugin.gameDataFiles[data.world.name] = newData
        save = true
    }

    fun removeGameData(data: GameData) {
        plugin.gameDataFiles[data.world.name]?.file?.delete()
        plugin.gameDataFiles.remove(data.world.name)
        save = true
    }

    fun getGameData(world: String): GameData? = plugin.gameDataFiles[world]?.get()

    fun getGameDatas(): Set<GameData> = plugin.gameDataFiles
        .filter { Bukkit.getWorld(it.key) != null }
        .map(Map.Entry<String, JsonFile<GameData>>::value)
        .map(JsonFile<GameData>::get)
        .toSet()

    @Suppress("unused")
    fun setLobby(location: Location?) {
        data.lobby = location
        save = true
    }

    fun getLobby(): Location? = data.lobby

    fun isEditing(data: GameData) = this.data.editing.contains(data.world.name)

    fun setEditing(data: GameData, editing: Boolean) {
        if (!editing) {
            this.data.editing.remove(data.world.name)
            save = true
            return
        }
        this.data.editing.add(data.world.name)
        save = true
        return
    }
}