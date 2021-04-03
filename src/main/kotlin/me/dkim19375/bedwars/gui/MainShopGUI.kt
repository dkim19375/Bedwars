package me.dkim19375.bedwars.gui

import me.dkim19375.bedwars.BedwarsPlugin
import me.dkim19375.bedwars.util.ItemWrapper
import me.dkim19375.bedwars.util.getItemAmount
import me.mattstudios.mfgui.gui.components.util.ItemBuilder
import me.mattstudios.mfgui.gui.guis.Gui
import me.mattstudios.mfgui.gui.guis.GuiItem
import org.bukkit.ChatColor
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionType

@Suppress("MemberVisibilityCanBePrivate")
class MainShopGUI(private val player: Player, private val plugin: BedwarsPlugin) {
    val mainScreen = Gui(6, "Quick Buy")

    private fun reset() {
        for (i in 0..53) {
            mainScreen.removeItem(i)
        }
    }

    private fun getItemBuilderForTop(material: Material): ItemBuilder {
        return ItemBuilder.from(material).setLore("${ChatColor.YELLOW}Click to view!")
    }

    private fun getItemBuilderForTop(item: ItemStack): ItemBuilder {
        return ItemBuilder.from(item).setLore("${ChatColor.YELLOW}Click to view!")
    }

    @Suppress("DEPRECATION")
    private fun setTopRow() {
        // top slots
        val quickBuyItem = getItemBuilderForTop(Material.NETHER_STAR)
            .setName("${ChatColor.AQUA}Quick Buy")
            .asGuiItem {
                showQuickBuy()
            }
        val blocksItem = getItemBuilderForTop(Material.HARD_CLAY)
            .setName("${ChatColor.GREEN}Blocks")
            .asGuiItem {
                showBlocks()
            }
        val meleeItem = getItemBuilderForTop(Material.HARD_CLAY)
            .setName("${ChatColor.GREEN}Melee")
            .asGuiItem {
                showMelee()
            }
        val armorItem = getItemBuilderForTop(Material.HARD_CLAY)
            .setName("${ChatColor.GREEN}Armor")
            .asGuiItem {
                showArmor()
            }
        val toolsItem = getItemBuilderForTop(Material.HARD_CLAY)
            .setName("${ChatColor.GREEN}Tools")
            .asGuiItem {
                showTools()
            }
        val rangedItem = getItemBuilderForTop(Material.HARD_CLAY)
            .setName("${ChatColor.GREEN}Ranged")
            .asGuiItem {
                showRanged()
            }
        val potionsItem = getItemBuilderForTop(Material.HARD_CLAY)
            .setName("${ChatColor.GREEN}Potions")
            .asGuiItem {
                showPotions()
            }
        val utilityItem = getItemBuilderForTop(Material.HARD_CLAY)
            .setName("${ChatColor.GREEN}Utility")
            .asGuiItem {
                showUtility()
            }
        val grayGlass = ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.GRAY.data.toShort())
        mainScreen.filler.fillBetweenPoints(
            2, 1, 2, 9, ItemBuilder.from(grayGlass)
                .setName("${ChatColor.DARK_GRAY}\u2191 ${ChatColor.GRAY}Categories")
                .setLore("${ChatColor.DARK_GRAY}\u2193 ${ChatColor.GRAY}Items")
                .asGuiItem()
        )
        mainScreen.update()
    }

    @Suppress("DEPRECATION")
    private fun putGreenGlass(col: Int) {
        val glass = ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.GREEN.data.toShort())
        mainScreen.setItem(
            2, col, ItemBuilder.from(glass)
                .setName("${ChatColor.DARK_GRAY}\u2191 ${ChatColor.GRAY}Categories")
                .setLore("${ChatColor.DARK_GRAY}\u2193 ${ChatColor.GRAY}Items")
                .asGuiItem()
        )
    }

    private fun getQuickBuyItem(slot: Int): GuiItem {
        val item = plugin.dataFileManager.getQuickBuySlot(slot, player.uniqueId)
        if (item == null) {
            @Suppress("DEPRECATION")
            val glass = ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.data.toShort())
            return ItemBuilder.from(glass)
                .setName("${ChatColor.RED}Empty slot!")
                .setLore(
                    "${ChatColor.GRAY}This is a Quick Buy Slot!",
                    "${ChatColor.AQUA}Sneak Click ${ChatColor.GRAY}any item in",
                    "${ChatColor.GRAY}the shop to add it here."
                )
                .asGuiItem()
        }

    }

    private fun formatItem(item: Items): GuiItem {
        val itemstack: ItemStack
        val team = plugin.gameManager.getTeamOfPlayer(player)
        itemstack = if (team == null) {
            item.item.toItemStack(null)
        } else {
            item.item.toItemStack(team.color)
        }
        val amount = player.getItemAmount(item.costType)
        if (amount >= item.costAmount) {
            // has enough

        }
    }

    @Suppress("DEPRECATION")
    fun showQuickBuy() {
        reset()
        setTopRow()
        putGreenGlass(1)
        mainScreen.updateTitle("Quick Buy")
        mainScreen.update()
    }

    fun showBlocks() {
        reset()
        setTopRow()
        putGreenGlass(2)
        mainScreen.updateTitle("Blocks")
        mainScreen.update()
    }

    fun showMelee() {
        reset()
        setTopRow()
        putGreenGlass(3)
        mainScreen.updateTitle("Melee")
        mainScreen.update()
    }

    fun showArmor() {
        reset()
        setTopRow()
        putGreenGlass(4)
        mainScreen.updateTitle("Armor")
        mainScreen.update()
    }

    fun showTools() {
        reset()
        setTopRow()
        putGreenGlass(5)
        mainScreen.updateTitle("Tools")
        mainScreen.update()
    }

    fun showRanged() {
        reset()
        setTopRow()
        putGreenGlass(6)
        mainScreen.updateTitle("Ranged")
        mainScreen.update()
    }

    fun showPotions() {
        reset()
        setTopRow()
        putGreenGlass(7)
        mainScreen.updateTitle("Potions")
        mainScreen.update()
    }

    fun showUtility() {
        reset()
        setTopRow()
        putGreenGlass(8)
        mainScreen.updateTitle("Utility")
        mainScreen.update()
    }

    enum class ItemType {
        BLOCKS,
        MELEE,
        ARMOR,
        TOOLS,
        RANGED,
        POTIONS,
        UTILITY
    }

    @Suppress("unused")
    enum class Items(
        val slot: Int, val item: ItemWrapper, val costAmount: Int, val costType: Material,
        val permanent: Boolean = false, val defaultOnSpawn: Boolean = false,
        val type: ItemType
    ) {
        WOOL(19, ItemWrapper(Material.WOOL, 16), 4, Material.IRON_INGOT, type = ItemType.BLOCKS),
        HARDENED_CLAY(20, ItemWrapper(Material.HARD_CLAY, 16), 12, Material.IRON_INGOT, type = ItemType.BLOCKS),
        BLAST_PROOF_GLASS(21, ItemWrapper(Material.GLASS, 4), 12, Material.IRON_INGOT, type = ItemType.BLOCKS),
        END_STONE(22, ItemWrapper(Material.ENDER_STONE, 12), 24, Material.IRON_INGOT, type = ItemType.BLOCKS),
        WOOD(23, ItemWrapper(Material.WOOD, 16), 16, Material.GOLD_INGOT, type = ItemType.BLOCKS),
        OBSIDIAN(24, ItemWrapper(Material.OBSIDIAN, 4), 4, Material.EMERALD, type = ItemType.BLOCKS),
        WOOD_SWORD(
            -1, ItemWrapper(Material.WOOD_SWORD, 1), 0, Material.IRON_INGOT, permanent = true,
            defaultOnSpawn = true, ItemType.MELEE
        ),
        STONE_SWORD(19, ItemWrapper(Material.STONE_SWORD, 1), 10, Material.IRON_INGOT, type = ItemType.MELEE),
        IRON_SWORD(20, ItemWrapper(Material.IRON_SWORD, 1), 7, Material.GOLD_INGOT, type = ItemType.MELEE),
        KB_STICK(
            21, ItemWrapper(Material.STICK, 1, enchants = listOf(Enchantment.KNOCKBACK)), 10, Material.GOLD_INGOT,
            permanent = true, type = ItemType.MELEE
        ),
        LEATHER_ARMOR(
            -1, ItemWrapper(Material.LEATHER_BOOTS, 1), 0, Material.IRON_INGOT, permanent = true,
            defaultOnSpawn = true, type = ItemType.ARMOR
        ),
        CHAIN_ARMOR(
            19,
            ItemWrapper(Material.CHAINMAIL_BOOTS, 1),
            40,
            Material.IRON_INGOT,
            permanent = true,
            type = ItemType.ARMOR
        ),
        IRON_ARMOR(
            20,
            ItemWrapper(Material.IRON_BOOTS, 1),
            12,
            Material.GOLD_INGOT,
            permanent = true,
            type = ItemType.ARMOR
        ),
        DIAMOND_ARMOR(
            21,
            ItemWrapper(Material.DIAMOND_BOOTS, 1),
            6,
            Material.EMERALD,
            permanent = true,
            type = ItemType.ARMOR
        ),
        SHEARS(19, ItemWrapper(Material.SHEARS, 1), 30, Material.IRON_INGOT, permanent = true, type = ItemType.TOOLS),
        WOOD_PICK(
            20,
            ItemWrapper(Material.WOOD_PICKAXE, 1),
            10,
            Material.IRON_INGOT,
            permanent = true,
            type = ItemType.TOOLS
        ),
        STONE_PICK(21, ItemWrapper(Material.STONE_PICKAXE, 1), 10, Material.IRON_INGOT, type = ItemType.TOOLS),
        IRON_PICKAXE(22, ItemWrapper(Material.IRON_PICKAXE, 1), 3, Material.GOLD_INGOT, type = ItemType.TOOLS),
        DIAMOND_PICKAXE(23, ItemWrapper(Material.DIAMOND_PICKAXE, 1), 6, Material.GOLD_INGOT, type = ItemType.TOOLS),
        WOOD_AXE(24, ItemWrapper(Material.WOOD_AXE, 1), 10, Material.IRON_INGOT, permanent = true, type = ItemType.TOOLS),
        STONE_AXE(25, ItemWrapper(Material.STONE_AXE, 1), 10, Material.IRON_INGOT, type = ItemType.TOOLS),
        IRON_AXE(28, ItemWrapper(Material.IRON_AXE, 1), 3, Material.GOLD_INGOT, type = ItemType.TOOLS),
        DIAMOND_AXE(29, ItemWrapper(Material.DIAMOND_AXE, 1), 6, Material.GOLD_INGOT, type = ItemType.TOOLS),
        ARROW(19, ItemWrapper(Material.ARROW, 8), 2, Material.GOLD_INGOT, type = ItemType.RANGED),
        BOW(19, ItemWrapper(Material.BOW, 1), 12, Material.GOLD_INGOT, type = ItemType.RANGED),
        POWER_BOW(
            19,
            ItemWrapper(Material.BOW, 1, enchants = listOf(Enchantment.ARROW_DAMAGE)),
            24,
            Material.GOLD_INGOT,
            type = ItemType.RANGED
        ),
        PUNCH_BOW(
            19, ItemWrapper(Material.BOW, 1, enchants = listOf(Enchantment.ARROW_DAMAGE, Enchantment.ARROW_KNOCKBACK)),
            6, Material.EMERALD, type = ItemType.RANGED
        ),
        SPEED(19, ItemWrapper(Material.POTION, 1, PotionType.SPEED, 2), 1, Material.EMERALD, type = ItemType.POTIONS),
        JUMP(20, ItemWrapper(Material.POTION, 1, PotionType.JUMP, 5), 1, Material.EMERALD, type = ItemType.POTIONS),
        INVISIBILITY(21, ItemWrapper(Material.POTION, 1, PotionType.INVISIBILITY, 1), 2, Material.EMERALD, type = ItemType.POTIONS),
        PEARL(19, ItemWrapper(Material.ENDER_PEARL, 1), 4, Material.EMERALD, type = ItemType.UTILITY),
        GOLDEN_APPLE(20, ItemWrapper(Material.GOLDEN_APPLE, 1), 3, Material.GOLD_INGOT, type = ItemType.UTILITY),
        FIREBALL(21, ItemWrapper(Material.FIREBALL, 1), 40, Material.FIREBALL, type = ItemType.UTILITY),
        TNT(22, ItemWrapper(Material.TNT, 1), 4, Material.GOLD_INGOT, type = ItemType.UTILITY),
        WATER_BUCKET(23, ItemWrapper(Material.WATER_BUCKET, 1), 1, Material.EMERALD, type = ItemType.UTILITY);

        companion object {
            fun getByMaterial(material: Material): Items? {
                values().forEach { i ->
                    if (i.item.material == material) {
                        return i
                    }
                }
                return null
            }

            fun getByType(type: ItemType): Set<Items> {
                val set = mutableSetOf<Items>()
                values().forEach { i ->
                    if (i.type == type) {
                        set.add(i)
                    }
                }
                return set.toSet()
            }

            fun fromString(str: String?): Items? {
                str?: return null
                return try {
                    valueOf(str)
                } catch (_: IllegalArgumentException) {
                    return null
                }
            }
        }
    }
}