package me.dkim19375.bedwars.plugin.enumclass

import me.dkim19375.bedwars.plugin.gui.MainShopGUI
import me.dkim19375.bedwars.plugin.data.ItemWrapper
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.potion.PotionType

@Suppress("unused")
enum class MainShopItems(
    val slot: Int, val item: ItemWrapper, val costAmount: Int, val costType: MainShopGUI.CostType,
    val displayname: String, val permanent: Boolean = false, val defaultOnSpawn: Boolean = false,
    val type: MainShopGUI.ItemType
) {
    WOOL(
        19, ItemWrapper(Material.WOOL, 16), 4, MainShopGUI.CostType.IRON, "Wool",
        type = MainShopGUI.ItemType.BLOCKS
    ),
    HARDENED_CLAY(
        20, ItemWrapper(Material.HARD_CLAY, 16), 12, MainShopGUI.CostType.IRON,
        "Clay", type = MainShopGUI.ItemType.BLOCKS
    ),
    BLAST_PROOF_GLASS(
        21, ItemWrapper(Material.GLASS, 4), 12, MainShopGUI.CostType.IRON,
        "Blast Proof Glass", type = MainShopGUI.ItemType.BLOCKS
    ),
    END_STONE(
        22, ItemWrapper(Material.ENDER_STONE, 12), 24, MainShopGUI.CostType.IRON,
        "Endstone", type = MainShopGUI.ItemType.BLOCKS
    ),
    WOOD(
        23, ItemWrapper(Material.WOOD, 16), 4, MainShopGUI.CostType.GOLD, "Wood",
        type = MainShopGUI.ItemType.BLOCKS
    ),
    OBSIDIAN(
        24, ItemWrapper(Material.OBSIDIAN, 4), 4, MainShopGUI.CostType.EMERALD, "Obsidian",
        type = MainShopGUI.ItemType.BLOCKS
    ),
    WOOD_SWORD(
        -1, ItemWrapper(Material.WOOD_SWORD, 1), 0, MainShopGUI.CostType.IRON, "Wooden Sword",
        permanent = true,
        defaultOnSpawn = true, MainShopGUI.ItemType.MELEE
    ),
    STONE_SWORD(
        19, ItemWrapper(Material.STONE_SWORD, 1), 10, MainShopGUI.CostType.IRON,
        "Stone Sword", type = MainShopGUI.ItemType.MELEE
    ),
    IRON_SWORD(
        20, ItemWrapper(Material.IRON_SWORD, 1), 7, MainShopGUI.CostType.GOLD,
        "Iron Sword", type = MainShopGUI.ItemType.MELEE
    ),
    KB_STICK(
        21,
        ItemWrapper(Material.STICK, 1, enchants = listOf(Enchantment.KNOCKBACK)),
        10,
        MainShopGUI.CostType.GOLD,
        "Stick (Knockback I)",
        permanent = true,
        type = MainShopGUI.ItemType.MELEE
    ),
    LEATHER_ARMOR(
        -1, ItemWrapper(Material.LEATHER_BOOTS, 1), 0, MainShopGUI.CostType.IRON,
        "Leather Armor", permanent = true,
        defaultOnSpawn = true, type = MainShopGUI.ItemType.ARMOR
    ),
    CHAIN_ARMOR(
        19,
        ItemWrapper(Material.CHAINMAIL_BOOTS, 1),
        40,
        MainShopGUI.CostType.IRON, "Chainmail Armor",
        permanent = true,
        type = MainShopGUI.ItemType.ARMOR
    ),
    IRON_ARMOR(
        20,
        ItemWrapper(Material.IRON_BOOTS, 1),
        12,
        MainShopGUI.CostType.GOLD, "Iron Armor",
        permanent = true,
        type = MainShopGUI.ItemType.ARMOR
    ),
    DIAMOND_ARMOR(
        21,
        ItemWrapper(Material.DIAMOND_BOOTS, 1),
        6,
        MainShopGUI.CostType.EMERALD, "Diamond Armor",
        permanent = true,
        type = MainShopGUI.ItemType.ARMOR
    ),
    SHEARS(
        19,
        ItemWrapper(Material.SHEARS, 1),
        30,
        MainShopGUI.CostType.IRON,
        "Shears",
        permanent = true,
        type = MainShopGUI.ItemType.TOOLS
    ),
    WOOD_PICK(
        20,
        ItemWrapper(Material.WOOD_PICKAXE, 1),
        10,
        MainShopGUI.CostType.IRON, "Wooden Pickaxe",
        permanent = true,
        type = MainShopGUI.ItemType.TOOLS
    ),
    STONE_PICK(
        21, ItemWrapper(Material.STONE_PICKAXE, 1), 10, MainShopGUI.CostType.IRON,
        "Stone Pickaxe", type = MainShopGUI.ItemType.TOOLS
    ),
    IRON_PICKAXE(
        22, ItemWrapper(Material.IRON_PICKAXE, 1), 3, MainShopGUI.CostType.GOLD,
        "Iron Pickaxe", type = MainShopGUI.ItemType.TOOLS
    ),
    DIAMOND_PICKAXE(
        23,
        ItemWrapper(Material.DIAMOND_PICKAXE, 1),
        6,
        MainShopGUI.CostType.GOLD,
        "Diamond Pickaxe",
        type = MainShopGUI.ItemType.TOOLS
    ),
    WOOD_AXE(
        24,
        ItemWrapper(Material.WOOD_AXE, 1),
        10,
        MainShopGUI.CostType.IRON,
        "Wooden Axe",
        permanent = true,
        type = MainShopGUI.ItemType.TOOLS
    ),
    STONE_AXE(
        25, ItemWrapper(Material.STONE_AXE, 1), 10, MainShopGUI.CostType.IRON,
        "Stone Axe", type = MainShopGUI.ItemType.TOOLS
    ),
    IRON_AXE(
        28, ItemWrapper(Material.IRON_AXE, 1), 3, MainShopGUI.CostType.GOLD,
        "Iron Axe", type = MainShopGUI.ItemType.TOOLS
    ),
    DIAMOND_AXE(
        29, ItemWrapper(Material.DIAMOND_AXE, 1), 6, MainShopGUI.CostType.GOLD,
        "Diamond Axe", type = MainShopGUI.ItemType.TOOLS
    ),
    ARROW(
        19, ItemWrapper(Material.ARROW, 8), 2, MainShopGUI.CostType.GOLD,
        "Arrow", type = MainShopGUI.ItemType.RANGED
    ),
    BOW(
        19, ItemWrapper(Material.BOW, 1), 12, MainShopGUI.CostType.GOLD,
        "Bow", type = MainShopGUI.ItemType.RANGED
    ),
    POWER_BOW(
        19,
        ItemWrapper(Material.BOW, 1, enchants = listOf(Enchantment.ARROW_DAMAGE)),
        24,
        MainShopGUI.CostType.GOLD, "Bow (Power I)",
        type = MainShopGUI.ItemType.RANGED
    ),
    PUNCH_BOW(
        19, ItemWrapper(Material.BOW, 1, enchants = listOf(Enchantment.ARROW_DAMAGE, Enchantment.ARROW_KNOCKBACK)),
        6, MainShopGUI.CostType.EMERALD, "Bow (Power I, Punch I)", type = MainShopGUI.ItemType.RANGED
    ),
    SPEED(
        19,
        ItemWrapper(Material.POTION, 1, PotionType.SPEED, 2),
        1,
        MainShopGUI.CostType.EMERALD,
        "Speed II Potion",
        type = MainShopGUI.ItemType.POTIONS
    ),
    JUMP(
        20,
        ItemWrapper(Material.POTION, 1, PotionType.JUMP, 5),
        1,
        MainShopGUI.CostType.EMERALD,
        "Jump V Potion",
        type = MainShopGUI.ItemType.POTIONS
    ),
    INVISIBILITY(
        21,
        ItemWrapper(Material.POTION, 1, PotionType.INVISIBILITY, 1),
        2,
        MainShopGUI.CostType.EMERALD,
        "Invisibility Potion",
        type = MainShopGUI.ItemType.POTIONS
    ),
    PEARL(
        19, ItemWrapper(Material.ENDER_PEARL, 1), 4, MainShopGUI.CostType.EMERALD,
        "Ender Pearl", type = MainShopGUI.ItemType.UTILITY
    ),
    GOLDEN_APPLE(
        20,
        ItemWrapper(Material.GOLDEN_APPLE, 1),
        3,
        MainShopGUI.CostType.GOLD,
        "Golden Apple",
        type = MainShopGUI.ItemType.UTILITY
    ),
    FIREBALL(
        21, ItemWrapper(Material.FIREBALL, 1), 40, MainShopGUI.CostType.IRON,
        "Fireball", type = MainShopGUI.ItemType.UTILITY
    ),
    TNT(
        22, ItemWrapper(Material.TNT, 1), 4, MainShopGUI.CostType.GOLD,
        "TNT", type = MainShopGUI.ItemType.UTILITY
    ),
    WATER_BUCKET(
        23, ItemWrapper(Material.WATER_BUCKET, 1), 1, MainShopGUI.CostType.EMERALD,
        "Water Bucket", type = MainShopGUI.ItemType.UTILITY
    );

    companion object {
        fun getByMaterial(material: Material): MainShopItems? {
            values().forEach { i ->
                if (i.item.material == material) {
                    return i
                }
            }
            return null
        }

        fun getByType(type: MainShopGUI.ItemType): Set<MainShopItems> {
            val set = mutableSetOf<MainShopItems>()
            values().forEach { i ->
                if (i.type == type) {
                    set.add(i)
                }
            }
            return set.toSet()
        }

        fun fromString(str: String?): MainShopItems? {
            str ?: return null
            return try {
                valueOf(str)
            } catch (_: IllegalArgumentException) {
                return null
            }
        }
    }
}