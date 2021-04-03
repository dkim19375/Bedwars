package me.dkim19375.bedwars.manager

import me.dkim19375.bedwars.BedwarsPlugin
import me.dkim19375.bedwars.gui.MainShopGUI
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
}