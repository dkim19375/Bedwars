package me.dkim19375.bedwars.plugin.gui

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.enumclass.Team
import me.dkim19375.bedwars.plugin.enumclass.TrapType
import me.dkim19375.bedwars.plugin.util.*
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
        reset()
        val grayGlass = ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.GRAY.data.toShort())
        menu.setItem(4, 4, getTrapItemOnMainScreen(1))
        menu.setItem(4, 5, getTrapItemOnMainScreen(2))
        menu.setItem(4, 6, getTrapItemOnMainScreen(3))
        menu.filler.fillBetweenPoints(
            3, 1, 3, 9, ItemBuilder.from(grayGlass)
                .setName("${ChatColor.DARK_GRAY}\u2191 ${ChatColor.GRAY}Purchasable")
                .setLore("${ChatColor.DARK_GRAY}\u2193 ${ChatColor.GRAY}Traps Queue")
                .asGuiItem {
                    it.isCancelled = true
                }
        )
        plugin.gameManager.getGame(player)?.let { game ->
            val upgrades = game.upgradesManager
            val sharpness = upgrades.sharpness.contains(team)
            val protection = upgrades.protection[team]
            val haste = upgrades.haste[team]
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
                )
            val armor = ItemBuilder.from(Material.IRON_CHESTPLATE)
                .setName(
                    "${(protection.zeroNonNull() >= 4).getGreenOrRed()}Reinforced Armor ${
                        (protection.zeroNonNull() + 1).limit(
                            4
                        ).toRomanNumeral()
                    }"
                )
                .setLore(
                    "Your team permanently gains".setGray(),
                    "Protection on all armor pieces!".setGray(),
                    " ",
                    "${(protection.zeroNonNull() >= 1).getGreenOrGray()}Tier 1: Protection I, ${ChatColor.AQUA}2 Diamonds",
                    "${(protection.zeroNonNull() >= 2).getGreenOrGray()}Tier 2: Protection II, ${ChatColor.AQUA}4 Diamonds",
                    "${(protection.zeroNonNull() >= 3).getGreenOrGray()}Tier 3: Protection III, ${ChatColor.AQUA}8 Diamonds",
                    "${(protection.zeroNonNull() >= 4).getGreenOrGray()}Tier 4: Protection IV, ${ChatColor.AQUA}16 Diamonds",
                    " ",
                    (if (protection.zeroNonNull() >= 4) "${ChatColor.GREEN}UNLOCKED" else {
                        "${ChatColor.YELLOW}Click to purchase!"
                    })
                )
            val maniacMiner = ItemBuilder.from(Material.GOLD_PICKAXE)
                .setName(
                    "${(haste.zeroNonNull() >= 4).getGreenOrRed()}Maniac Miner ${
                        (haste.zeroNonNull() + 1).limit(2).toRomanNumeral()
                    }"
                )
                .setLore(
                    "All players on your team".setGray(),
                    "permanently gain Haste.".setGray(),
                    " ",
                    "${(haste.zeroNonNull() >= 1).getGreenOrGray()}Tier 1: Haste I, ${ChatColor.AQUA}2 Diamonds",
                    "${(haste.zeroNonNull() >= 2).getGreenOrGray()}Tier 2: Haste II, ${ChatColor.AQUA}4 Diamonds",
                    " ",
                    (if (haste.zeroNonNull() >= 2) "${ChatColor.GREEN}UNLOCKED" else {
                        "${ChatColor.YELLOW}Click to purchase!"
                    })
                )
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
                )
            val buyTrapItem = ItemBuilder.from(Material.LEATHER)
                .setName("${ChatColor.YELLOW}Buy a trap")
                .setLore(
                    "Purchased traps will be".setGray(),
                    "queued on the right.".setGray(),
                    " ",
                    "${ChatColor.YELLOW}Click to browse!"
                )
            menu.setItem(2, 3, sharpSword.asGuiItem { event ->
                event.isCancelled = true
            })
            menu.setItem(2, 4, armor.asGuiItem { event ->
                event.isCancelled = true
            })
            menu.setItem(2, 5, maniacMiner.asGuiItem { event ->
                event.isCancelled = true
            })
            menu.setItem(2, 6, healPoolItem.asGuiItem { event ->
                event.isCancelled = true
            })
            menu.setItem(2, 7, buyTrapItem.asGuiItem { event ->
                event.isCancelled = true
            })
        }
        menu.updateTitle("Upgrades & Traps")
        menu.update()
    }


    fun showTrapScreen() {

    }
}