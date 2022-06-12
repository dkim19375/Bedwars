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

package me.dkim19375.bedwars.plugin.gui

import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import me.dkim19375.bedwars.plugin.manager.BedwarsGame
import me.dkim19375.bedwars.plugin.util.*
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player

class CompassGUI(private val player: Player, private val game: BedwarsGame) {
    private val menu: Gui = Gui.gui()
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
            .lore(listOf(
                "Purchase tracking upgrade".setGray(),
                "for your compass which will".setGray(),
                "track each player on a".setGray(),
                "specific team until you".setGray(),
                "die.".setGray(),
                " ",
                "${ChatColor.YELLOW}Click to open!"
            )).addAllFlags()
            .asNewGuiItem {
                TrackerGUI(player, game).showPlayer()
            }
        menu.setItem(2, 5, shopItem)
    }
}