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
import me.dkim19375.dkimbukkitcore.function.getPlayers
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.entity.Player
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class TeleporterGUI(private val player: Player, private val game: BedwarsGame) {
    val menu: Gui = Gui.gui()
        .rows(min(9, max(1, ceil(game.data.maxPlayers.toDouble() / 9.0).toInt())))
        .title("Teleporter".toComponent())
        .disableAllInteractions()
        .create()

    private fun reset() {
        for (i in 0 until menu.rows * 9) {
            menu.removeItem(i)
        }
    }

    fun showPlayer() {
        showMenu()
        menu.open(player)
    }

    private fun showMenu() {
        reset()
        for (player in game.getPlayersInGame().getPlayers().filter {
            it.gameMode != GameMode.SPECTATOR
        }) {
            val item = ItemBuilder.skull()
                .owner(player)
                .name(player.displayName)
                .lore(
                    "Health: ${ChatColor.WHITE}${(player.health * 5).toInt()}%".setGray(),
                    " ",
                    "Click to teleport to the player!".setGray()
                ).addAllFlags()
                .asNewGuiItem {
                    this.player.teleport(player)
                    menu.close(this.player)
                }
            menu.addItem(item)
        }
    }
}