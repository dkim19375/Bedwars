package me.dkim19375.bedwars.compat.abstract

import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

@Suppress("PropertyName")
abstract class NBTUtilitiesAbstract {

    companion object {
        fun getInstance(plugin: JavaPlugin): NBTUtilitiesAbstract = if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_16_R1)) {
            Class.forName("me.dkim19375.bedwars.v1_16.NBTUtilities")
                .getConstructor(JavaPlugin::class.java)
                .newInstance(plugin) as NBTUtilitiesAbstract
        } else {
            Class.forName("me.dkim19375.bedwars.v1_8.NBTUtilities")
                .getConstructor()
                .newInstance() as NBTUtilitiesAbstract
        }
    }

    protected val HOLOGRAM_KEY = "BedwarsArmorStand"
    protected val CONFIG_ITEM_KEY = "BedwarsConfigItem"
    protected val BEDWARS_BLOCK_KEY = "BedwarsPlacedBlock"

    abstract fun <T : LivingEntity> addAI(entity: T)

    abstract fun <T : LivingEntity> removeAI(entity: T)

    abstract fun setNBTData(itemStack: ItemStack, item: String?): ItemStack

    // custom

    abstract fun getConfigItem(item: ItemStack): String?

    abstract fun isHologram(armorStand: ArmorStand): Boolean

    abstract fun setHologramNBT(armorStand: ArmorStand, holo: Boolean): ArmorStand
}