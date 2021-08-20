package me.dkim19375.bedwars.plugin.gui

import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import me.dkim19375.bedwars.plugin.enumclass.getColored
import me.dkim19375.bedwars.plugin.manager.BedwarsGame
import me.dkim19375.bedwars.plugin.util.*
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class TrackerGUI(private val player: Player, private val game: BedwarsGame) {
    val menu: Gui = Gui.gui()
        .rows(4)
        .title("Purchase Enemy Tracker".toComponent())
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
        val items = mutableListOf<GuiItem>()
        val hasEnough = player.getItemAmount(Material.EMERALD) >= 2
        for (team in game.beds.filter { (_, active) -> active }.keys
            .filter { game.getTeamOfPlayer(player) != it }
            .filter { game.trackers[player.uniqueId] != it }) {
            items.add(
                ItemBuilder.from(team.getColored(Material.WOOL))
                    .name("${hasEnough.getGreenOrRed()}Track Team ${team.displayName}")
                    .lore(
                        "Purchase tracking upgrade".setGray(),
                        "for your compass which will".setGray(),
                        "track each player on a".setGray(),
                        "specific team until you".setGray(),
                        "die.".setGray(),
                        " ",
                        "Cost: ${ChatColor.DARK_GREEN}2 Emeralds".setGray(),
                        " ",
                        if (hasEnough) {
                            "${ChatColor.YELLOW}Click to purchase!"
                        } else {
                            "${ChatColor.RED}You do not have enough ${MainShopGUI.CostType.EMERALD.color}Emeralds!"
                        }
                    ).flags(*ItemFlag.values())
                    .asGuiItem {
                        val amount = player.getItemAmount(Material.EMERALD)
                        if (amount < 2) {
                            player.sendMessage("${ChatColor.RED}You need ${2 - amount} more Emeralds!")
                            player.playErrorSound()
                            return@asGuiItem
                        }
                        player.sendMessage("${ChatColor.GREEN}You purchased ${ChatColor.GOLD}${team.displayName} Tracking")
                        player.sendMessage("${ChatColor.RED}You will lose ability to track this team when you die!")
                        player.inventory.removeItem(ItemStack(Material.EMERALD, 2))
                        game.trackers[player.uniqueId] = team
                    }
            )
        }
        if (items.isEmpty()) {
            return
        }
        for (i in 2..(items.size + 1)) {
            menu.setItem(2, i, items[i - 2])
        }
    }
}