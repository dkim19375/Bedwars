package me.dkim19375.bedwars.v1_8

import de.tr7zw.changeme.nbtapi.NBTCompound
import de.tr7zw.changeme.nbtapi.NBTEntity
import de.tr7zw.changeme.nbtapi.NBTItem
import de.tr7zw.changeme.nbtapi.NBTTileEntity
import de.tr7zw.nbtinjector.NBTInjector
import me.dkim19375.bedwars.compat.abstract.NBTUtilitiesAbstract
import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.data.MainShopConfigItem
import org.bukkit.block.BlockState
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack

private const val NO_AI_KEY = "NoAI"

class NBTUtilities(plugin: BedwarsPlugin) : NBTUtilitiesAbstract(plugin) {
    private fun <T : LivingEntity> T.getNBT(): NBTCompound {
        val patched = NBTInjector.patchEntity(this)
        return NBTInjector.getNbtData(patched)
    }

    private fun NBTCompound.getStringOrNull(key: String): String? = if (keys.contains(key)) getString(key) else null

    private fun <T : Entity> T.getVanillaNBT(): NBTCompound = NBTEntity(this)

    private fun ItemStack.getNBT(): NBTItem = NBTItem(this, true)

    override fun <T : LivingEntity> addAI(entity: T): Unit = entity.getVanillaNBT().setInteger(NO_AI_KEY, 0)

    override fun <T : LivingEntity> removeAI(entity: T): Unit = entity.getVanillaNBT().setInteger(NO_AI_KEY, 1)

    override fun setNBTData(itemStack: ItemStack, item: String?): ItemStack {
        item ?: return itemStack
        return itemStack.getNBT().apply { setString(BEDWARS_BLOCK_KEY, item) }.item
    }

    override fun getConfigItem(item: ItemStack): MainShopConfigItem? =
        plugin.configManager.getItemFromName(item.getNBT().getStringOrNull(CONFIG_ITEM_KEY))

    override fun isHologram(armorStand: ArmorStand): Boolean = armorStand.getNBT().keys.contains(HOLOGRAM_KEY)

    override fun setHologramNBT(armorStand: ArmorStand, holo: Boolean) {
        if (holo) {
            armorStand.getNBT().setByte(HOLOGRAM_KEY, 0)
        } else {
            armorStand.getNBT().removeKey(HOLOGRAM_KEY)
        }
    }
}