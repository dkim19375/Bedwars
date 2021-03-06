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

package me.dkim19375.bedwars.v1_16

import me.dkim19375.bedwars.compat.abstract.NBTUtilitiesAbstract
import org.bukkit.NamespacedKey
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

@Suppress("PrivatePropertyName", "unused")
class NBTUtilities(plugin: JavaPlugin) : NBTUtilitiesAbstract() {

    private val CONFIG_ITEM_NS_KEY = NamespacedKey(plugin, CONFIG_ITEM_KEY)
    private val HOLOGRAM_NS_KEY = NamespacedKey(plugin, HOLOGRAM_KEY)
    private val MOB_DROP_NS_KEY = NamespacedKey(plugin, MOB_DROP_KEY)
    private val GEN_DROP_NS_KEY = NamespacedKey(plugin, GEN_DROP_KEY)
    private val TRACKER_NS_KEY = NamespacedKey(plugin, TRACKER_KEY)
    private val PDT_STRING: PersistentDataType<String, String>
        get() = PersistentDataType.STRING
    private val PDT_BYTE: PersistentDataType<Byte, Byte>
        get() = PersistentDataType.BYTE

    private fun <T : PersistentDataHolder> T.modifyByte(key: NamespacedKey, add: Boolean = true): T = apply {
        if (add) {
            persistentDataContainer.set(key, PDT_BYTE, 0)
        } else {
            persistentDataContainer.remove(key)
        }
    }

    private fun <T : PersistentDataHolder> T.hasKey(key: NamespacedKey): Pair<Boolean, T> =
        persistentDataContainer.keys.contains(key) to this

    override fun <T : LivingEntity> addAI(entity: T): Unit = entity.setAI(true)

    override fun <T : LivingEntity> removeAI(entity: T): Unit = entity.setAI(false)

    override fun setConfigItem(itemStack: ItemStack, item: String?): ItemStack {
        item ?: return itemStack
        itemStack.itemMeta = itemStack.itemMeta?.apply {
            persistentDataContainer.set(CONFIG_ITEM_NS_KEY, PDT_STRING, item)
        }
        return itemStack
    }

    override fun getConfigItem(item: ItemStack): String? =
        item.itemMeta?.persistentDataContainer?.get(CONFIG_ITEM_NS_KEY, PDT_STRING)

    override fun isHologram(armorStand: ArmorStand): Boolean = armorStand.hasKey(HOLOGRAM_NS_KEY).first

    override fun setHologramNBT(armorStand: ArmorStand, holo: Boolean): ArmorStand = armorStand.modifyByte(HOLOGRAM_NS_KEY, holo)

    override fun setUnbreakable(item: ItemStack, unbreakable: Boolean): ItemStack = item.apply {
        itemMeta = itemMeta?.apply { isUnbreakable = true }
    }

    override fun setDrop(item: Item, drop: Boolean): Item = item.modifyByte(GEN_DROP_NS_KEY, drop)

    override fun isDrop(item: Item): Pair<Boolean, Item> = item.hasKey(GEN_DROP_NS_KEY)

    override fun <T : Entity> disableDrops(entity: T): T = entity.modifyByte(MOB_DROP_NS_KEY)

    override fun <T : Entity> isDropsDisabled(entity: T): Pair<Boolean, T> = entity.hasKey(MOB_DROP_NS_KEY)
}