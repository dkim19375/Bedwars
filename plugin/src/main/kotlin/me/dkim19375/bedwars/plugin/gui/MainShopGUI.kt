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
import me.dkim19375.bedwars.plugin.data.MainShopConfigItem
import me.dkim19375.bedwars.plugin.enumclass.ArmorType
import me.dkim19375.bedwars.plugin.manager.BedwarsGame
import me.dkim19375.bedwars.plugin.util.*
import me.dkim19375.dkimbukkitcore.function.formatAll
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

@Suppress("MemberVisibilityCanBePrivate")
class MainShopGUI(private val player: Player, private val plugin: BedwarsPlugin, private val game: BedwarsGame) {
    private val configManager = plugin.shopConfigManager
    val menu: Gui = Gui.gui()
        .rows(6)
        .title("Quick Buy".toComponent())
        .disableAllInteractions()
        .create()
    var isSettingQuickBuy = false
    lateinit var quickBuyItem: MainShopConfigItem

    private fun reset() {
        for (i in 0..53) {
            menu.removeItem(i)
        }
    }

    fun showPlayer() {
        showQuickBuy()
        menu.open(player)
    }

    private fun getItemBuilderForTop(material: Material): ItemBuilder {
        return ItemBuilder.from(material).lore("${ChatColor.YELLOW}Click to view!")
    }

    @Suppress("DEPRECATION")
    private fun setTopRow() {
        menu.setItem(0, getGuiItem("${ChatColor.AQUA}Quick Buy", this::showQuickBuy, Material.NETHER_STAR))
        menu.setItem(2, getGuiItem("Blocks", this::showBlocks))
        menu.setItem(3, getGuiItem("Melee", this::showMelee, Material.GOLD_SWORD))
        menu.setItem(4, getGuiItem("Armor", this::showArmor, Material.CHAINMAIL_BOOTS))
        menu.setItem(5, getGuiItem("Tools", this::showTools, Material.STONE_PICKAXE))
        menu.setItem(6, getGuiItem("Ranged", this::showRanged, Material.BOW))
        menu.setItem(7, getGuiItem("Potions", this::showPotions, Material.BREWING_STAND_ITEM))
        menu.setItem(8, getGuiItem("Utility", this::showUtility, Material.TNT))
        val grayGlass = ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.GRAY.data.toShort())
        menu.filler.fillBetweenPoints(
            2, 1, 2, 9, ItemBuilder.from(grayGlass)
                .setName("${ChatColor.DARK_GRAY}\u2191 ${ChatColor.GRAY}Categories")
                .addLore("${ChatColor.DARK_GRAY}\u2193 ${ChatColor.GRAY}Items")
                .asNewGuiItem()
        )
        menu.update()
    }

    private fun getGuiItem(name: String, whatToShow: () -> Unit, material: Material = Material.HARD_CLAY) =
        getItemBuilderForTop(material)
            .name("${ChatColor.GREEN}$name")
            .asNewGuiItem {
                checkIfSettingQuickBuy()
                whatToShow()
            }

    private fun checkIfSettingQuickBuy() {
        if (!isSettingQuickBuy) {
            return
        }
        player.sendMessage("${ChatColor.RED}Cancelled Quick Buy selection!")
        isSettingQuickBuy = false
    }

    @Suppress("DEPRECATION")
    private fun putGreenGlass(col: Int) {
        val glass = ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.GREEN.data.toShort())
        menu.setItem(
            2, col, ItemBuilder.from(glass)
                .setName("${ChatColor.DARK_GRAY}\u2191 ${ChatColor.GRAY}Categories")
                .addLore("${ChatColor.DARK_GRAY}\u2193 ${ChatColor.GRAY}Items")
                .asNewGuiItem()
        )
    }

    private fun getNoneInQuickBuyItem(): ItemStack {
        @Suppress("DEPRECATION")
        val glass = ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.data.toShort())
        glass.itemMeta?.let {
            it.displayName = "${ChatColor.RED}Empty slot!"
            val lore = if (it.lore == null) mutableListOf<String>() else it.lore
            lore.addAll(
                listOf(
                    "${ChatColor.GRAY}This is a Quick Buy Slot!",
                    "${ChatColor.AQUA}Sneak Click ${ChatColor.GRAY}any item in",
                    "${ChatColor.GRAY}the shop to add it here."
                )
            )
            glass.itemMeta = it
        }
        return glass
    }

    private fun getQuickBuyItem(slot: Int): GuiItem {
        val quickBuyAction: (Player) -> Unit = { player ->
            isSettingQuickBuy = false
            if (plugin.dataFileManager.getQuickBuySlot(slot, player.uniqueId) != null) {
                player.sendMessage("${ChatColor.RED}That slot is taken!")
            } else {
                plugin.dataFileManager.setQuickBuySlot(slot, player.uniqueId, quickBuyItem)
                player.sendMessage("${ChatColor.GREEN}Successfully set Quick Buy slot!")
                showQuickBuy()
            }
        }
        val item = plugin.dataFileManager.getQuickBuySlot(slot, player.uniqueId)
            ?: return ItemBuilder.from(getNoneInQuickBuyItem()).asNewGuiItem {
                if (isSettingQuickBuy) {
                    quickBuyAction(player)
                }
            }
        return formatItem(item, true)
            .lore("${ChatColor.AQUA}Sneak Click to remove from Quick Buy!")
            .asNewGuiItem { event ->
                event.isCancelled = true
                item.itemCategory.getShowMethod()
                val player = event.view.player as? Player ?: return@asNewGuiItem
                if (isSettingQuickBuy) {
                    quickBuyAction(player)
                    return@asNewGuiItem
                }
                if (!event.isShiftClick) {
                    onClick(item, event) {}
                    return@asNewGuiItem
                }
                plugin.dataFileManager.setQuickBuySlot(slot, player.uniqueId, null)
                menu.setItem(slot, ItemBuilder.from(getNoneInQuickBuyItem()).asNewGuiItem())
                menu.update()
                player.sendMessage("${ChatColor.GREEN}Successfully removed ${item.getFriendlyName()} from Quick Buy!")
            }
    }

    private fun givePlayerItem(item: MainShopConfigItem) {
        val game = plugin.gameManager.getGame(player) ?: return
        if (item.itemCategory == ItemType.ARMOR) {
            if (player.inventory.hasArmor(ArmorType.fromMaterial(item.item.material))) {
                player.sendMessage("${ChatColor.RED}You already have this!")
                player.playErrorSound()
                return
            }
            player.inventory.removeItem(ItemStack(item.costItem.material, item.cost))
            player.sendMessage("${ChatColor.GREEN}You purchased ${ChatColor.GOLD}${item.getFriendlyName()}!")
            val armorType = ArmorType.fromMaterial(item.item.material) ?: return
            player.inventory.boots = ItemStack(armorType.boots)
            player.inventory.leggings = ItemStack(armorType.leggings)
            game.upgradesManager.applyUpgrades(player)
            item.commands.map { it.formatAll(player) }.forEach { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), it) }
            player.playBoughtSound()
            return
        }
        if (player.inventory.hasItem(item.item.material) && (item.permanent || item.defaultOnSpawn)) {
            player.sendMessage("${ChatColor.RED}You already have this!")
            player.playErrorSound()
            return
        }
        val weightCategory = item.weightCategory
        val weightAutoRemove = item.weightAutoRemove
        val weightPrevent = item.weightPrevent
        if (weightCategory != null) {
            if (!plugin.shopConfigManager.canGetItem(player.inventory, item)) {
                if (!weightPrevent) {
                    return
                }
                player.sendMessage(item.weightMessage(player))
                player.playErrorSound()
                return
            }
            if (weightAutoRemove) {
                val items = plugin.shopConfigManager.getOtherItemsWithWeight(player.inventory, item)
                player.inventory.removeItem(*items.toTypedArray())
            }
        }
        player.inventory.removeItem(ItemStack(item.costItem.material, item.cost))
        val team = plugin.gameManager.getTeamOfPlayer(player)
        player.sendMessage("${ChatColor.GREEN}You purchased ${ChatColor.GOLD}${item.getFriendlyName()}!")
        player.giveItem(item.item.toItemStack(team?.color))
        game.upgradesManager.applyUpgrades(player)
        item.commands.map { it.formatAll(player) }.forEach { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), it) }
        player.playBoughtSound()
    }

    private fun getBuySlots(): List<Int> {
        val list = mutableListOf<Int>()
        list.addAll(19..25) // first row
        list.addAll(28..34) // second row
        list.addAll(37..43) // third row
        return list
    }

    private fun getRemainingQuickSlots(): Int {
        var amount = 0
        for (i in getBuySlots()) {
            if (plugin.dataFileManager.getQuickBuySlot(i, player.uniqueId) == null) {
                amount++
            }
        }
        return amount
    }

    private fun formatItem(item: MainShopConfigItem, quickBuy: Boolean = false): ItemBuilder {
        val team = plugin.gameManager.getTeamOfPlayer(player)
        val itemstack = item.item.toItemStack(team?.color)
        if (item.cosmetic) {
            return ItemBuilder.from(itemstack)
        }
        val amount = player.getItemAmount(item.costItem.material)
        val builder: ItemBuilder
        val loreToAdd = mutableListOf(
            "${ChatColor.GRAY}Cost: " +
                    "${item.costItem.color}${item.cost} ${item.costItem.displayname}",
            " "
        )
        if (item.permanent) {
            loreToAdd.addAll(
                listOf(
                    "This item is permanent! When you".setGray(),
                    "respawn, you will keep this item.".setGray(),
                    " "
                )
            )
        }
        if (item.item.material.isTool()) {
            loreToAdd.addAll(
                listOf(
                    "If you purchase this item, you".setGray(),
                    "will keep the lowest tier on death.".setGray(),
                    " "
                )
            )
        }
        if (!quickBuy) {
            loreToAdd.addAll(
                listOf(
                    "${ChatColor.AQUA}Sneak Click to add to Quick Buy",
                    " "
                )
            )
        }
        builder = if (amount >= item.cost) {
            ItemBuilder.from(itemstack)
                .name("${ChatColor.GREEN}${item.getFriendlyName()}")
                .lore(loreToAdd)
                .lore("${ChatColor.YELLOW}Click to purchase!")
        } else {
            ItemBuilder.from(itemstack)
                .name("${ChatColor.RED}${item.getFriendlyName()}")
                .lore(loreToAdd)
                .lore("${ChatColor.RED}You do not have enough ${item.costItem.color}${item.costItem.displayname}!")
        }
        game.upgradesManager.applyToItem(builder, player)
        if (getRemainingQuickSlots() < 1) {
            return builder
        }
        return builder.lore("")
    }

    private fun onClick(item: MainShopConfigItem, event: InventoryClickEvent, menuAction: (MainShopGUI) -> Unit) {
        event.isCancelled = true
        if (isSettingQuickBuy) {
            return
        }
        menuAction(this)
        val playerCostAmount = player.getItemAmount(item.costItem.material)
        if (playerCostAmount < item.cost) {
            player.sendMessage("${ChatColor.RED}You need ${item.cost - playerCostAmount} more ${item.costItem.displayname}!")
            player.playErrorSound()
            return
        }
        givePlayerItem(item)
    }

    private fun showShopScreen(col: Int, name: String, type: ItemType) {
        reset()
        setTopRow()
        putGreenGlass(col + 1)
        for (item in configManager.getByType(type)) {
            if (item.defaultOnSpawn) {
                continue
            }
            if (item.cosmetic) {
                menu.setItem(item.slot, formatItem(item).asNewGuiItem())
                continue
            }
            menu.setItem(item.slot, formatItem(item).asNewGuiItem {
                if (it.isShiftClick) {
                    if (getBuySlots().none { i ->
                            plugin.dataFileManager.getQuickBuySlot(i, player.uniqueId) == null
                        }) {
                        player.sendMessage("${ChatColor.RED}You have no available Quick Buy slots!")
                        return@asNewGuiItem
                    }
                    if (isSettingQuickBuy) {
                        checkIfSettingQuickBuy()
                        return@asNewGuiItem
                    }
                    showQuickBuy()
                    isSettingQuickBuy = true
                    quickBuyItem = item
                    player.sendMessage("${ChatColor.GREEN}Setting Quick Buy!")
                    return@asNewGuiItem
                }
                if (item.permanent) {
                    if (player.inventory.getAllContents().toList().filterNotNull().any { i ->
                            i.type == item.item.material
                        }) {
                        player.sendMessage("${ChatColor.RED}You already have that item!")
                        return@asNewGuiItem
                    }
                }
                onClick(item, it, item.itemCategory.getShowMethod())
            })
        }
        menu.updateTitle(name)
        menu.update()
    }

    fun showQuickBuy() {
        reset()
        setTopRow()
        putGreenGlass(1)
        getBuySlots().forEach { i ->
            menu.setItem(i, getQuickBuyItem(i))
        }
        menu.updateTitle("Quick Buy")
        menu.update()
    }

    fun showBlocks() {
        showShopScreen(2, "Blocks", ItemType.BLOCKS)
    }

    fun showMelee() {
        showShopScreen(3, "Melee", ItemType.MELEE)
    }

    fun showArmor() {
        showShopScreen(4, "Armor", ItemType.ARMOR)
    }

    fun showTools() {
        showShopScreen(5, "Tools", ItemType.TOOLS)
    }

    fun showRanged() {
        showShopScreen(6, "Ranged", ItemType.RANGED)
    }

    fun showPotions() {
        showShopScreen(7, "Potions", ItemType.POTIONS)
    }

    fun showUtility() {
        showShopScreen(8, "Utility", ItemType.UTILITY)
    }

    enum class ItemType {
        BLOCKS,
        MELEE,
        ARMOR,
        TOOLS,
        RANGED,
        POTIONS,
        UTILITY;

        fun getShowMethod(): (MainShopGUI) -> Unit = when (this) {
            BLOCKS -> MainShopGUI::showBlocks
            MELEE -> MainShopGUI::showMelee
            ARMOR -> MainShopGUI::showArmor
            TOOLS -> MainShopGUI::showTools
            RANGED -> MainShopGUI::showRanged
            POTIONS -> MainShopGUI::showPotions
            UTILITY -> MainShopGUI::showUtility
        }
    }

    @Suppress("unused") // the enum values are still being used, but not the value names ("IRON", "GOLD", etc)
    enum class CostType(val material: Material, val color: ChatColor, val displayname: String) {
        IRON(Material.IRON_INGOT, ChatColor.WHITE, "Iron"),
        GOLD(Material.GOLD_INGOT, ChatColor.GOLD, "Gold"),
        EMERALD(Material.EMERALD, ChatColor.GREEN, "Emerald")
    }
}

fun Material.toCostType(): MainShopGUI.CostType? {
    for (cost in MainShopGUI.CostType.values()) {
        if (cost.material == this) {
            return cost
        }
    }
    return null
}