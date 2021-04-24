package me.dkim19375.bedwars.plugin.gui

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.enumclass.ArmorType
import me.dkim19375.bedwars.plugin.enumclass.MainShopItems
import me.dkim19375.bedwars.plugin.util.*
import me.mattstudios.mfgui.gui.components.util.ItemBuilder
import me.mattstudios.mfgui.gui.guis.Gui
import me.mattstudios.mfgui.gui.guis.GuiItem
import org.bukkit.ChatColor
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
class MainShopGUI(private val player: Player, private val plugin: BedwarsPlugin) {
    val menu = Gui(6, "Quick Buy")
    var isSettingQuickBuy = false

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
        return ItemBuilder.from(material).addLore("${ChatColor.YELLOW}Click to view!")
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
                .asGuiItem {
                    it.isCancelled = true
                }
        )
        menu.update()
    }

    private fun getGuiItem(name: String, whatToShow: () -> Unit, material: Material = Material.HARD_CLAY) =
        getItemBuilderForTop(material)
            .setName("${ChatColor.GREEN}$name")
            .asGuiItem {
                it.isCancelled = true
                checkIfSettingQuickBuy()
                whatToShow()
            }

    private fun checkIfSettingQuickBuy() {
        if (!isSettingQuickBuy) return
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
                .asGuiItem {
                    it.isCancelled = true
                }
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
        val item = plugin.dataFileManager.getQuickBuySlot(slot, player.uniqueId)
            ?: return ItemBuilder.from(getNoneInQuickBuyItem()).asGuiItem {
                it.isCancelled = true
            }
        return formatItem(item)
            .addLore("${ChatColor.AQUA}Sneak Click to remove from Quick Buy!")
            .asGuiItem { event ->
                event.isCancelled = true
                item.type.getShowMethod()
                if (event.view.player !is Player) {
                    return@asGuiItem
                }
                val player = event.view.player as Player
                if (isSettingQuickBuy) {
                    if (plugin.dataFileManager.getQuickBuySlot(slot, player.uniqueId) != null) {
                        player.sendMessage("${ChatColor.RED}That slot is taken!")
                        isSettingQuickBuy = false
                        return@asGuiItem
                    }
                    plugin.dataFileManager.setQuickBuySlot(slot, player.uniqueId, item)
                    player.sendMessage("${ChatColor.GREEN}Successfully set Quick Buy slot!")
                    isSettingQuickBuy = false
                    return@asGuiItem
                }
                if (!event.isShiftClick) {
                    givePlayerItem(item)
                    return@asGuiItem
                }
                plugin.dataFileManager.setQuickBuySlot(slot, player.uniqueId, null)
                menu.setItem(slot, ItemBuilder.from(getNoneInQuickBuyItem()).asGuiItem {
                    it.isCancelled = true
                })
                player.sendMessage("${ChatColor.GREEN}Successfully removed ${item.displayname} from Quick Buy!")
            }
    }

    private fun givePlayerItem(item: MainShopItems) {
        if (item.type == ItemType.ARMOR) {
            if (player.inventory.hasArmor(ArmorType.fromMaterial(item.item.material))) {
                player.sendMessage("${ChatColor.RED}You already have this!")
                player.playSound(Sound.NOTE_PLING, pitch = 2.0f)
                return
            }
            player.inventory.removeItem(ItemStack(item.costType.material, item.costAmount))
            player.sendMessage("${ChatColor.GREEN}Successfully bought ${item.displayname}!")
            val armorType = ArmorType.fromMaterial(item.item.material)?: return
            player.inventory.boots = ItemStack(armorType.boots)
            player.inventory.leggings = ItemStack(armorType.leggings)
            return
        }
        if (player.inventory.hasItem(item.item.material) && (item.permanent || item.defaultOnSpawn)) {
            player.sendMessage("${ChatColor.RED}You already have this!")
            player.playSound(Sound.NOTE_PLING, pitch = 2.0f)
            return
        }
        player.inventory.removeItem(ItemStack(item.costType.material, item.costAmount))
        val team = plugin.gameManager.getTeamOfPlayer(player)
        player.sendMessage("${ChatColor.GREEN}Successfully bought ${item.displayname}!")
        if (team == null) {
            player.inventory.addItem(item.item.toItemStack(team?.color))
            return
        }
        player.inventory.addItem(item.item.toItemStack(team.color))
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

    private fun formatItem(item: MainShopItems): ItemBuilder {
        val team = plugin.gameManager.getTeamOfPlayer(player)
        val itemstack = item.item.toItemStack(team?.color)
        val amount = player.getItemAmount(item.costType.material)
        val builder: ItemBuilder
        if (amount >= item.costAmount) {
            builder = ItemBuilder.from(itemstack)
                .setName("${ChatColor.GREEN}${item.displayname}")
                .addLore(
                    "${ChatColor.GRAY}Cost: " +
                            "${item.costType.color}${item.costAmount} ${item.costType.displayname}"
                )
                .addLore(" ", "${ChatColor.YELLOW}Click to purchase!")
        } else {
            builder = ItemBuilder.from(itemstack)
                .setName("${ChatColor.RED}${item.displayname}")
                .addLore(
                    "${ChatColor.GRAY}Cost: " +
                            "${item.costType.color}${item.costAmount} ${item.costType.displayname}"
                )
                .addLore(
                    " ",
                    "${ChatColor.RED}You do not have enough ${item.costType.color}${item.costType.displayname}!"
                )
        }
        if (getRemainingQuickSlots() < 1) {
            return builder
        }
        return builder.addLore("")
    }

    private fun onClick(item: MainShopItems, event: InventoryClickEvent, menuAction: (MainShopGUI) -> Unit) {
        event.isCancelled = true
        if (isSettingQuickBuy) {
            return
        }
        menuAction(this)
        val playerCostAmount = player.getItemAmount(item.costType.material)
        if (playerCostAmount < item.costAmount) {
            player.sendMessage("${ChatColor.RED}You need ${item.costAmount - playerCostAmount} more ${item.costType.displayname}!")
            player.playSound(Sound.ANVIL_LAND, pitch = 0.8f)
            return
        }
        player.playSound(Sound.NOTE_PLING)
        givePlayerItem(item)
    }

    private fun showShopScreen(col: Int, name: String, type: ItemType) {
        reset()
        setTopRow()
        putGreenGlass(col)
        for (item in MainShopItems.values()) {
            if (item.defaultOnSpawn) {
                continue
            }
            if (item.type != type) {
                continue
            }
            menu.setItem(item.slot, formatItem(item).asGuiItem {
                it.isCancelled = true
                if (it.isShiftClick) {
                    if (getBuySlots().none { i ->
                            plugin.dataFileManager.getQuickBuySlot(
                                i,
                                player.uniqueId
                            ) == null
                        }) {
                        player.sendMessage("${ChatColor.RED}You have no available Quick Buy slots!")
                        return@asGuiItem
                    }

                    return@asGuiItem
                }
                if (item.permanent) {
                    if (player.inventory.contents.filter(Objects::nonNull).any { i ->
                            i.type == item.item.material
                        }) {
                        player.sendMessage("${ChatColor.RED}You already have that item!")
                        return@asGuiItem
                    }
                }
                onClick(item, it, item.type.getShowMethod())
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

    enum class CostType(val material: Material, val color: ChatColor, val displayname: String) {
        IRON(Material.IRON_INGOT, ChatColor.WHITE, "Iron"),
        GOLD(Material.GOLD_INGOT, ChatColor.GOLD, "Gold"),
        EMERALD(Material.EMERALD, ChatColor.GREEN, "Emerald")
    }
}