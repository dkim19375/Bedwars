package me.dkim19375.bedwars.plugin.manager

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.gui.MainShopGUI
import me.dkim19375.bedwars.plugin.data.GameData
import org.bukkit.Bukkit
import java.util.*

class DataFileManager(private val plugin: BedwarsPlugin) {
    var save = false

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

    fun getQuickBuySlot(slot: Int, player: UUID): MainShopGUI.Items? {
        return MainShopGUI.Items.fromString(plugin.dataFile.config.getString("$player.shop.quick-buy.$slot"))
    }

    fun setQuickBuySlot(slot: Int, player: UUID, item: MainShopGUI.Items?) {
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

    fun getGameDatas(): Set<GameData> {
        val section = plugin.dataFile.config.getConfigurationSection("game-data")?: return emptySet()
        val dataSet = mutableSetOf<GameData>()
        for (key in section.getKeys(false)) {
            if (Bukkit.getWorld(key) == null) {
                continue
            }
            val data = section.get(key)?: continue
            if (data !is GameData) {
                continue
            }
            dataSet.add(data)
        }
        return dataSet.toSet()
    }
}