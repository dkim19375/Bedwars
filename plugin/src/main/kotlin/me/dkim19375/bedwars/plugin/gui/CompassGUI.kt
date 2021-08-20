package me.dkim19375.bedwars.plugin.gui

import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import me.dkim19375.bedwars.plugin.manager.BedwarsGame
import me.dkim19375.bedwars.plugin.util.lore
import me.dkim19375.bedwars.plugin.util.name
import me.dkim19375.bedwars.plugin.util.setGray
import me.dkim19375.bedwars.plugin.util.toComponent
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag

class CompassGUI(private val player: Player, private val game: BedwarsGame) {
    val menu: Gui = Gui.gui()
        .rows(3)
        .title("Tracker & Communication".toComponent())
        .disableAllInteractions()
        .create()

    private fun reset() {
        for (i in 0 until menu.rows * 9) {
            menu.removeItem(i)
        }
    }

    fun showPlayer() {
        showMain()
        menu.open(player)
    }

    private fun showMain() {
        reset()
        val shopItem = ItemBuilder.from(Material.COMPASS)
            .name("${ChatColor.GREEN}Tracker Shop")
            .lore(
                "Purchase tracking upgrade".setGray(),
                "for your compass which will".setGray(),
                "track each player on a".setGray(),
                "specific team until you".setGray(),
                "die.".setGray(),
                " ",
                "${ChatColor.YELLOW}Click to open!"
            ).flags(*ItemFlag.values())
            .asGuiItem {
                TrackerGUI(player, game).showPlayer()
            }
        menu.setItem(2, 5, shopItem)
    }
}