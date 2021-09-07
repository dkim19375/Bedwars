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
import dev.triumphteam.gui.guis.GuiItem
import me.dkim19375.bedwars.plugin.BedwarsPlugin
import  me.dkim19375.bedwars.api.enumclass.Team
import me.dkim19375.bedwars.plugin.enumclass.TrapType
import me.dkim19375.bedwars.api.enumclass.UpgradeType
import me.dkim19375.bedwars.plugin.manager.BedwarsGame
import me.dkim19375.bedwars.plugin.util.*
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class UpgradeShopGUI(private val player: Player, private val team: Team, private val plugin: BedwarsPlugin) {
    val menu: Gui = Gui.gui()
        .rows(5)
        .title("Upgrades & Traps".toComponent())
        .disableAllInteractions()
        .create()

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
        showMainScreen()
    }

    private fun reset() {
        for (i in 0..44) {
            menu.removeItem(i)
        }
    }

    private fun getTrapItemOnMainScreen(number: Int): GuiItem {
        val default =
            @Suppress("deprecation")
            ItemBuilder.from(ItemStack(Material.STAINED_GLASS_PANE, number, DyeColor.GRAY.data.toShort()))
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
                ).addAllFlags()
                .asNewGuiItem { event ->
                    event.isCancelled = true
                    showMainScreen()
                }
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
        val lore = when (trapType) {
            TrapType.ALARM -> alarmLore
            TrapType.COUNTER_OFFENSIVE -> counterOffensiveLore
            TrapType.ITS_A_TRAP -> itsATrapLore
            TrapType.MINER_FATIGUE -> fatigueLore
        }.plus(
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
        return ItemBuilder.from(ItemStack(trapType.material, number))
            .name("${ChatColor.GREEN}Trap #$number: ${trapType.displayName} Trap")
            .lore(lore)
            .addAllFlags()
            .asNewGuiItem { event ->
                event.isCancelled = true
                showMainScreen()
            }
    }

    @Suppress("deprecation")
    fun showMainScreen() {
        reset()
        menu.updateTitle("Upgrades & Traps")
        val grayGlass = ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.GRAY.data.toShort())
        menu.setItem(4, 4, getTrapItemOnMainScreen(1))
        menu.setItem(4, 5, getTrapItemOnMainScreen(2))
        menu.setItem(4, 6, getTrapItemOnMainScreen(3))
        menu.filler.fillBetweenPoints(
            3, 1, 3, 9, ItemBuilder.from(grayGlass)
                .setName("${ChatColor.DARK_GRAY}\u2191 ${ChatColor.GRAY}Purchasable")
                .setLore("${ChatColor.DARK_GRAY}\u2193 ${ChatColor.GRAY}Traps Queue")
                .addAllFlags()
                .asNewGuiItem {
                    it.isCancelled = true
                }
        )
        plugin.gameManager.getGame(player)?.let { game ->
            val upgrades = game.upgradesManager
            val sharpness = upgrades.sharpness.contains(team)
            val protection = upgrades.protection[team] ?: 0
            val haste = upgrades.haste[team] ?: 0
            val healPool = upgrades.healPool.contains(team)

            // upgrades
            val sharpSword = ItemBuilder.from(Material.IRON_SWORD)
                .setName("${sharpness.getGreenOrRed()}Sharpened Swords")
                .setLore(
                    "Your team permanently gains".setGray(),
                    "Sharpness I on all swords and".setGray(),
                    "axes!".setGray(),
                    " ",
                    "Cost: ${ChatColor.AQUA}4 Diamonds".setGray(),
                    " ",
                    (if (sharpness) "${ChatColor.GREEN}UNLOCKED" else {
                        "${ChatColor.YELLOW}Click to purchase!"
                    })
                ).addAllFlags()
            val armor = ItemBuilder.from(Material.IRON_CHESTPLATE)
                .setName(
                    "${(protection >= 4).getGreenOrRed()}Reinforced Armor ${
                        (protection + 1).coerceAtMost(4).toRomanNumeral()
                    }"
                ).setLore(
                    "Your team permanently gains".setGray(),
                    "Protection on all armor pieces!".setGray(),
                    " ",
                    "${(protection >= 1).getGreenOrGray()}Tier 1: Protection I, ${ChatColor.AQUA}2 Diamonds",
                    "${(protection >= 2).getGreenOrGray()}Tier 2: Protection II, ${ChatColor.AQUA}4 Diamonds",
                    "${(protection >= 3).getGreenOrGray()}Tier 3: Protection III, ${ChatColor.AQUA}8 Diamonds",
                    "${(protection >= 4).getGreenOrGray()}Tier 4: Protection IV, ${ChatColor.AQUA}16 Diamonds",
                    " ",
                    (if (protection >= 4) "${ChatColor.GREEN}UNLOCKED" else {
                        "${ChatColor.YELLOW}Click to purchase!"
                    })
                ).amount((protection + 1).coerceAtMost(4))
                .addAllFlags()
            val maniacMiner = ItemBuilder.from(Material.GOLD_PICKAXE)
                .setName(
                    "${(haste >= 4).getGreenOrRed()}Maniac Miner ${
                        (haste + 1).coerceAtMost(2).toRomanNumeral()
                    }"
                )
                .setLore(
                    "All players on your team".setGray(),
                    "permanently gain Haste.".setGray(),
                    " ",
                    "${(haste >= 1).getGreenOrGray()}Tier 1: Haste I, ${ChatColor.AQUA}2 Diamonds",
                    "${(haste >= 2).getGreenOrGray()}Tier 2: Haste II, ${ChatColor.AQUA}4 Diamonds",
                    " ",
                    (if (haste >= 2) "${ChatColor.GREEN}UNLOCKED" else {
                        "${ChatColor.YELLOW}Click to purchase!"
                    })
                ).amount((haste + 1).coerceAtMost(2))
                .addAllFlags()
            val healPoolItem = ItemBuilder.from(Material.BEACON)
                .setName("${healPool.getGreenOrRed()}Heal Pool")
                .setLore(
                    "Creates a Regeneration field".setGray(),
                    "around your base!".setGray(),
                    " ",
                    "Cost: ${ChatColor.AQUA}1 Diamond".setGray(),
                    " ",
                    (if (healPool) "${ChatColor.GREEN}UNLOCKED" else {
                        "${ChatColor.YELLOW}Click to purchase!"
                    })
                ).addAllFlags()
            val buyTrapItem = ItemBuilder.from(Material.LEATHER)
                .setName("${ChatColor.YELLOW}Buy a trap")
                .setLore(
                    "Purchased traps will be".setGray(),
                    "queued on the right.".setGray(),
                    " ",
                    "${ChatColor.YELLOW}Click to browse!"
                ).addAllFlags()
            menu.setItem(2, 3, sharpSword.asNewGuiItem { event ->
                event.isCancelled = true
                onClick(UpgradeType.SHARPNESS)
            })
            menu.setItem(2, 4, armor.asNewGuiItem { event ->
                event.isCancelled = true
                onClick(UpgradeType.PROTECTION)
            })
            menu.setItem(2, 5, maniacMiner.asNewGuiItem { event ->
                event.isCancelled = true
                onClick(UpgradeType.HASTE)
            })
            menu.setItem(2, 6, healPoolItem.asNewGuiItem { event ->
                event.isCancelled = true
                onClick(UpgradeType.HEAL_POOL)
            })
            menu.setItem(2, 7, buyTrapItem.asNewGuiItem { event ->
                event.isCancelled = true
                showTrapScreen()
            })
        }
        menu.updateTitle("Upgrades & Traps")
        menu.update()
    }

    private fun showTrapScreen() {
        reset()
        val game = plugin.gameManager.getGame(player) ?: return
        val upgrades = game.upgradesManager

        val diamonds = player.getItemAmount(Material.DIAMOND)
        val level = upgrades.getLevel(team)
        val cost = level + 1
        val hasEnough = diamonds >= cost
        val enoughStr = hasEnoughBool(hasEnough)

        val itsATrap =
            getTrapItem(
                cost,
                enoughStr,
                Material.TRIPWIRE_HOOK,
                itsATrapLore,
                TrapType.ITS_A_TRAP.displayName,
                TrapType.ITS_A_TRAP
            )
        val counterOffensive =
            getTrapItem(
                cost,
                enoughStr,
                Material.FEATHER,
                counterOffensiveLore,
                TrapType.COUNTER_OFFENSIVE.displayName,
                TrapType.COUNTER_OFFENSIVE
            )
        val alarm = getTrapItem(
            cost,
            enoughStr,
            Material.REDSTONE_TORCH_ON,
            alarmLore,
            TrapType.ALARM.displayName,
            TrapType.ALARM
        )
        val mining = getTrapItem(
            cost,
            enoughStr,
            Material.IRON_PICKAXE,
            fatigueLore,
            TrapType.MINER_FATIGUE.displayName,
            TrapType.MINER_FATIGUE
        )
        menu.setItem(2, 2, itsATrap)
        menu.setItem(2, 3, counterOffensive)
        menu.setItem(2, 4, alarm)
        menu.setItem(2, 5, mining)
        menu.updateTitle("Queue a trap")
        menu.update()
    }

    private fun onTrapClick(trap: TrapType) {
        val game = plugin.gameManager.getGame(player) ?: return
        val upgrades = game.upgradesManager
        if (upgrades.thirdTrap.containsKey(team)) {
            player.sendMessage("${ChatColor.RED}You already have the maximum amount of traps!")
            player.playErrorSound()
            return
        }
        val diamonds = player.getItemAmount(Material.DIAMOND)
        val level = upgrades.getLevel(team)
        val cost = level + 1
        if (diamonds < cost) {
            player.sendMessage("${ChatColor.RED}You need ${cost - diamonds} more Diamond!")
            player.playErrorSound()
            return
        }
        player.inventory.removeItem(ItemStack(Material.DIAMOND, cost))
        upgrades.addTrap(team, trap)
        sendUpgradeMessage(trap.displayName, game)
    }

    private fun getTrapItem(
        cost: Int,
        hasEnough: String,
        material: Material,
        firstLore: List<String>,
        name: String,
        type: TrapType,
    ): GuiItem = ItemBuilder.from(material)
        .name("${ChatColor.RED}$name")
        .lore(firstLore.plus(getCostList(cost, hasEnough)))
        .addAllFlags()
        .asNewGuiItem {
            it.isCancelled = true
            onTrapClick(type)
            showTrapScreen()
        }

    private fun getCostList(cost: Int, hasEnough: String): List<String> = listOf(
        " ",
        "Cost: ${ChatColor.AQUA}$cost Diamond".setGray(),
        " ",
        hasEnough
    )

    private fun hasEnoughBool(bool: Boolean): String {
        if (bool) {
            return "${ChatColor.YELLOW}Click to purchase!"
        }
        return "${ChatColor.RED}You don't have enough Diamonds!"
    }

    private fun onClick(type: UpgradeType) {
        val game = plugin.gameManager.getGame(player) ?: return
        val upgrades = game.upgradesManager

        when (type) {
            UpgradeType.SHARPNESS -> {
                if (!verifyItems(upgrades.sharpness.contains(team), 4)) {
                    return
                }
                player.inventory.removeItem(ItemStack(Material.DIAMOND, 4))
                upgrades.sharpness.add(team)
                sendUpgradeMessage("Sharpness", game)
                game.upgradesManager.applyUpgrades(player)
                showMainScreen()
                return
            }
            UpgradeType.PROTECTION -> {
                if (!verifyHasPurchase(upgrades.protection.getOrDefault(team, 0) >= 4)) {
                    showMainScreen()
                    return
                }
                val level = upgrades.protection.getOrDefault(team, 0)
                val cost = ProtectionLevel.fromInt(level + 1).cost
                chargeUpgradable(upgrades.protection, cost, game, "Reinforced Armor")
                game.upgradesManager.applyUpgrades(player)
                showMainScreen()
                return
            }
            UpgradeType.HASTE -> {
                if (!verifyHasPurchase(upgrades.haste.getOrDefault(team, 0) >= 2)) {
                    showMainScreen()
                    return
                }
                val level = upgrades.haste.getOrDefault(team, 0)
                val cost = (level + 1) * 2
                chargeUpgradable(upgrades.haste, cost, game, "Maniac Miner")
                game.upgradesManager.applyUpgrades(player)
                showMainScreen()
                return
            }
            UpgradeType.HEAL_POOL -> {
                showMainScreen()
                if (!verifyItems(upgrades.healPool.contains(team), 1)) {
                    return
                }
                upgrades.healPool.add(team)
                player.inventory.removeItem(ItemStack(Material.DIAMOND, 1))
                sendUpgradeMessage("Heal Pool", game)
                game.upgradesManager.applyUpgrades(player)
                showMainScreen()
                return
            }
        }
    }

    private fun chargeUpgradable(map: MutableMap<Team, Int>, cost: Int, game: BedwarsGame, name: String) {
        val level = map.getOrDefault(team, 0)
        val playerItems = player.getItemAmount(Material.DIAMOND)
        if (playerItems < cost) {
            player.sendMessage("${ChatColor.RED}You need ${cost - playerItems} more Diamond!")
            player.playErrorSound()
            return
        }
        map[team] = level + 1
        player.inventory.removeItem(ItemStack(Material.DIAMOND, cost))
        sendUpgradeMessage("$name ${(level + 1).toRomanNumeral()}", game)
        return
    }

    private fun verifyHasPurchase(hasUpgrade: Boolean): Boolean {
        return if (hasUpgrade) {
            player.sendMessage("${ChatColor.RED}You already have this purchased!")
            false
        } else true
    }

    private fun verifyItems(hasUpgrade: Boolean, cost: Int): Boolean {
        if (!verifyHasPurchase(hasUpgrade)) {
            return false
        }
        val playerItems = player.getItemAmount(Material.DIAMOND)
        if (playerItems < cost) {
            player.sendMessage("${ChatColor.RED}You need ${cost - playerItems} more Diamond!")
            player.playErrorSound()
            return false
        }
        return true
    }

    private fun sendUpgradeMessage(upgrade: String, game: BedwarsGame) {
        player.playBoughtSound()
        for (uuid in game.getPlayersInTeam(team)) {
            val p = Bukkit.getPlayer(uuid) ?: continue
            p.sendMessage("${player.displayName} ${ChatColor.GREEN}has purchased $upgrade!")
        }
    }

    enum class ProtectionLevel(val cost: Int) {
        ONE(2),
        TWO(4),
        THREE(8),
        FOUR(16);

        companion object {
            fun fromInt(int: Int): ProtectionLevel {
                return when (int) {
                    1 -> ONE
                    2 -> TWO
                    3 -> THREE
                    4 -> FOUR
                    else -> ONE
                }
            }
        }
    }
}