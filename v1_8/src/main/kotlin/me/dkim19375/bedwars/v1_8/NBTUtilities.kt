package me.dkim19375.bedwars.v1_8

import de.tr7zw.changeme.nbtapi.NBTCompound
import de.tr7zw.changeme.nbtapi.NBTEntity
import de.tr7zw.changeme.nbtapi.NBTItem
import de.tr7zw.nbtinjector.NBTInjector
import me.dkim19375.bedwars.compat.abstract.NBTUtilitiesAbstract
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack

private const val NO_AI_KEY = "NoAI"

@Suppress("unused", "UNCHECKED_CAST")
class NBTUtilities : NBTUtilitiesAbstract() {
    private fun <T : LivingEntity> T.getNBT(): Pair<T, NBTCompound> {
        val patched = NBTInjector.patchEntity(this) as T
        return patched to NBTInjector.getNbtData(patched)
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

    override fun getConfigItem(item: ItemStack): String? = item.getNBT().getStringOrNull(CONFIG_ITEM_KEY)

    override fun isHologram(armorStand: ArmorStand): Boolean = armorStand.getNBT().second.keys.contains(HOLOGRAM_KEY)

    override fun setHologramNBT(armorStand: ArmorStand, holo: Boolean): ArmorStand = if (holo) {
        armorStand.getNBT().let {
            it.second.setByte(HOLOGRAM_KEY, 0)
            it.first
        }
    } else {
        armorStand.getNBT().let {
            it.second.removeKey(HOLOGRAM_KEY)
            it.first
        }
    }
}