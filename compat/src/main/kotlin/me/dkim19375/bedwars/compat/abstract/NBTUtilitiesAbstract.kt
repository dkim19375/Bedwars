package me.dkim19375.bedwars.compat.abstract

import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion
import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.data.MainShopConfigItem
import me.dkim19375.bedwars.v1_8.NBTUtilities
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack

@Suppress("PropertyName")
abstract class NBTUtilitiesAbstract(protected val plugin: BedwarsPlugin) {

    companion object {
        fun getInstance(plugin: BedwarsPlugin): NBTUtilitiesAbstract = if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_16_R1)) {
            NBTUtilities(plugin)
        } else {
            me.dkim19375.bedwars.v1_16.NBTUtilities(plugin)
        }
    }

    protected val HOLOGRAM_KEY = "BedwarsArmorStand"
    protected val CONFIG_ITEM_KEY = "BedwarsConfigItem"
    protected val BEDWARS_BLOCK_KEY = "BedwarsPlacedBlock"

    abstract fun <T : LivingEntity> addAI(entity: T)

    abstract fun <T : LivingEntity> removeAI(entity: T)

    fun setNBTData(itemStack: ItemStack, item: MainShopConfigItem?): ItemStack = setNBTData(itemStack, item?.name)

    abstract fun setNBTData(itemStack: ItemStack, item: String?): ItemStack

    // custom

    abstract fun getConfigItem(item: ItemStack): MainShopConfigItem?

    abstract fun isHologram(armorStand: ArmorStand): Boolean

    abstract fun setHologramNBT(armorStand: ArmorStand, holo: Boolean)
}