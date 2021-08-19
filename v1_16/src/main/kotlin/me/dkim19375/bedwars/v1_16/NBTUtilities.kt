package me.dkim19375.bedwars.v1_16

import me.dkim19375.bedwars.compat.abstract.NBTUtilitiesAbstract
import org.bukkit.NamespacedKey
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

@Suppress("PrivatePropertyName", "unused")
class NBTUtilities(plugin: JavaPlugin) : NBTUtilitiesAbstract() {

    private val HOLOGRAM_NS_KEY = NamespacedKey(plugin, HOLOGRAM_KEY)
    private val CONFIG_ITEM_NS_KEY = NamespacedKey(plugin, CONFIG_ITEM_KEY)
    private val BEDWARS_BLOCK_NS_KEY = NamespacedKey(plugin, BEDWARS_BLOCK_KEY)
    private val PDT_STRING: PersistentDataType<String, String>
        get() = PersistentDataType.STRING
    private val PDT_BYTE: PersistentDataType<Byte, Byte>
        get() = PersistentDataType.BYTE

    override fun <T : LivingEntity> addAI(entity: T): Unit = entity.setAI(true)

    override fun <T : LivingEntity> removeAI(entity: T): Unit = entity.setAI(false)

    override fun setNBTData(itemStack: ItemStack, item: String?): ItemStack {
        item ?: return itemStack
        itemStack.itemMeta = itemStack.itemMeta?.apply {
            persistentDataContainer.set(BEDWARS_BLOCK_NS_KEY, PDT_STRING, item)
        }
        return itemStack
    }

    override fun getConfigItem(item: ItemStack): String? =
        item.itemMeta?.persistentDataContainer?.get(CONFIG_ITEM_NS_KEY, PDT_STRING)

    override fun isHologram(armorStand: ArmorStand): Boolean =
        armorStand.persistentDataContainer.keys.contains(HOLOGRAM_NS_KEY)

    override fun setHologramNBT(armorStand: ArmorStand, holo: Boolean): ArmorStand {
        if (holo) {
            armorStand.persistentDataContainer.set(HOLOGRAM_NS_KEY, PDT_BYTE, 0)
        }
        return armorStand
    }
}