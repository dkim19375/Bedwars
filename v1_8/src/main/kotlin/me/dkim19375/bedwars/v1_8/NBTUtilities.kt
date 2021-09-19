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

    private fun <T : Entity> T.modifyByte(key: String, add: Boolean = true): T = if (add) {
        getNBT().let {
            it.second.setByte(key, 0)
            it.first
        }
    } else {
        getNBT().let {
            it.second.removeKey(key)
            it.first
        }
    }

    private fun <T : Entity> T.hasByte(key: String): Pair<Boolean, T> = getNBT().let {
        it.second.keys.contains(key) to it.first
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

    override fun isHologram(armorStand: ArmorStand): Boolean = armorStand.hasByte(HOLOGRAM_KEY).first

    override fun setHologramNBT(armorStand: ArmorStand, holo: Boolean): ArmorStand =
        armorStand.modifyByte(HOLOGRAM_KEY, holo)

    override fun setUnbreakable(item: ItemStack, unbreakable: Boolean): ItemStack =
        item.getNBT().apply { setInteger("Unbreakable", if (unbreakable) 1 else 0) }.item

    override fun setDrop(item: Item, drop: Boolean): Item = item.modifyByte(GEN_DROP_KEY, drop)

    override fun isDrop(item: Item): Pair<Boolean, Item> = item.hasByte(GEN_DROP_KEY)

    override fun <T : Entity> disableDrops(entity: T): T = entity.modifyByte(MOB_DROP_KEY)

    override fun <T : Entity> isDropsDisabled(entity: T): Pair<Boolean, T> = entity.hasByte(MOB_DROP_KEY)
}