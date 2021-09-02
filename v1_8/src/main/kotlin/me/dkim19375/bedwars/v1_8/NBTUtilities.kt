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

package me.dkim19375.bedwars.v1_8

import de.tr7zw.changeme.nbtapi.NBTCompound
import de.tr7zw.changeme.nbtapi.NBTEntity
import de.tr7zw.changeme.nbtapi.NBTItem
import de.tr7zw.nbtinjector.NBTInjector
import me.dkim19375.bedwars.compat.abstract.NBTUtilitiesAbstract
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack

private const val NO_AI_KEY = "NoAI"

@Suppress("unused", "UNCHECKED_CAST")
class NBTUtilities : NBTUtilitiesAbstract() {
    private fun <T : Entity> T.getNBT(): Pair<T, NBTCompound> {
        val patched = NBTInjector.patchEntity(this) as T
        return patched to NBTInjector.getNbtData(patched)
    }

    private fun NBTCompound.getStringOrNull(key: String): String? = if (keys.contains(key)) getString(key) else null

    private fun <T : Entity> T.getVanillaNBT(): NBTCompound = NBTEntity(this)

    private fun ItemStack.getNBT(): NBTItem = NBTItem(this, true)

    override fun <T : LivingEntity> addAI(entity: T): Unit = entity.getVanillaNBT().setInteger(NO_AI_KEY, 0)

    override fun <T : LivingEntity> removeAI(entity: T): Unit = entity.getVanillaNBT().setInteger(NO_AI_KEY, 1)

    override fun setConfigItem(itemStack: ItemStack, item: String?): ItemStack {
        item ?: return itemStack
        return itemStack.getNBT().apply { setString(CONFIG_ITEM_KEY, item) }.item
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

    override fun setUnbreakable(item: ItemStack, unbreakable: Boolean): ItemStack =
        item.getNBT().apply { setInteger("Unbreakable", if (unbreakable) 1 else 0) }.item

    override fun setDrop(item: Item, drop: Boolean): Item = if (drop) {
        item.getNBT().let {
            it.second.setByte(DROP_KEY, 0)
            it.first
        }
    } else {
        item.getNBT().let {
            it.second.removeKey(DROP_KEY)
            it.first
        }
    }

    override fun isDrop(item: Item): Boolean = item.getNBT().second.keys.contains(DROP_KEY)
}