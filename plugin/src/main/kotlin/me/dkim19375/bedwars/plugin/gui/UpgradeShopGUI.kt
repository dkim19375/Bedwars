package me.dkim19375.bedwars.plugin.gui

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.enumclass.Team
import me.dkim19375.bedwars.plugin.enumclass.TrapType
import me.dkim19375.bedwars.plugin.util.addLore
import me.dkim19375.bedwars.plugin.util.combine
import me.mattstudios.mfgui.gui.components.util.ItemBuilder
import me.mattstudios.mfgui.gui.guis.Gui
import me.mattstudios.mfgui.gui.guis.GuiItem
import org.bukkit.ChatColor
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class UpgradeShopGUI(private val player: Player, private val team: Team, private val plugin: BedwarsPlugin) {
    private val menu = Gui(5, "Upgrades & Traps")

    private val itsATrapLore = listOf(
        "${ChatColor.GRAY}Inflicts Blindness and Slowness",
        "${ChatColor.GRAY}for 8 seconds."
    )
    private val counterOffensiveLore = listOf(
        "${ChatColor.GRAY}Grants Speed I and Jump Boost II",
        "${ChatColor.GRAY}for 10 seconds to allied players",
        "${ChatColor.GRAY}near your base."
    )
    private val alarmLore = listOf(
        "${ChatColor.GRAY}Reveals invisible players as",
        "${ChatColor.GRAY}well as their name and team."
    )
    private val fatigueLore = listOf(
        "${ChatColor.GRAY}Inflict Mining Fatigue for 10",
        "${ChatColor.GRAY}seconds."
    )

    fun showPlayer() {
        menu.open(player)
    }

    private fun reset() {
        for (i in 0..45) {
            menu.removeItem(i)
        }
    }

    private fun getTrapItemOnMainScreen(number: Int): GuiItem {
        val default =
            @Suppress("deprecation")
            ItemBuilder.from(ItemStack(Material.STAINED_GLASS_PANE, number, DyeColor.SILVER.data.toShort()))
                .setName("${ChatColor.RED}Trap #$number: No Trap!")
                .setLore(
                    "${ChatColor.GRAY}The${
                        when (number) {
                            1 -> " first"
                            2 -> " second"
                            3 -> " third"
                            else -> ""
                        }
                    } enemy to walk",
                    "${ChatColor.GRAY}into your base will trigger",
                    "${ChatColor.GRAY}this trap!",
                    " ",
                    "${ChatColor.GRAY}Purchasing a trap will",
                    "${ChatColor.GRAY}queue it here. Its cost",
                    "${ChatColor.GRAY}will scale based on the",
                    "${ChatColor.GRAY}number of traps queued.",
                    "${ChatColor.GRAY}Next trap: ${ChatColor.AQUA}$number Diamond${if (number == 1) "" else "s"}"
                )
                .asGuiItem { event -> event.isCancelled = true }
        val game = plugin.gameManager.getGame(player) ?: return default
        val trapType = when (number) {
            1 -> {
                game.upgradesManager.firstTrap[team]
            }
            2 -> {
                game.upgradesManager.secondTrap[team]
            }
            3 -> {
                game.upgradesManager.thirdTrap[team]
            }
            else -> {
                return default
            }
        } ?: return default
        return ItemBuilder.from(ItemStack(trapType.material, number))
            .setName("${ChatColor.GREEN}Trap #$number: ${trapType.displayName} Trap")
            .setLore(
                "${
                    when (trapType) {
                        TrapType.ALARM -> alarmLore
                        TrapType.COUNTER_OFFENSIVE -> counterOffensiveLore
                        TrapType.ITS_A_TRAP -> itsATrapLore
                        TrapType.MINER_FATIGUE -> fatigueLore
                    }.combine(
                        listOf(
                            " ",
                            "${ChatColor.GRAY}The${
                                when (number) {
                                    1 -> " first"
                                    2 -> " second"
                                    3 -> " third"
                                    else -> ""
                                }
                            } enemy to walk",
                            "${ChatColor.GRAY}into your base will trigger",
                            "${ChatColor.GRAY}this trap!"
                        )
                    )
                }"
            )
            .asGuiItem { event -> event.isCancelled = true }
    }

    @Suppress("deprecation")
    fun showMainScreen() {
        val grayGlass = ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.GRAY.data.toShort())
        menu.setItem(4, 4, getTrapItemOnMainScreen(1))
        menu.setItem(4, 5, getTrapItemOnMainScreen(2))
        menu.setItem(4, 6, getTrapItemOnMainScreen(3))
        menu.filler.fillBetweenPoints(
            3, 1, 3, 9, ItemBuilder.from(grayGlass)
                .setName("${ChatColor.DARK_GRAY}\u2191 ${ChatColor.GRAY}Purchasable")
                .addLore("${ChatColor.DARK_GRAY}\u2193 ${ChatColor.GRAY}Traps Queue")
                .asGuiItem {
                    it.isCancelled = true
                }
        )
        menu.update()
    }
}