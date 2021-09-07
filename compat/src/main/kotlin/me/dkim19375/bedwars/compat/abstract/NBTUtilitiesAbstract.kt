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

package me.dkim19375.bedwars.compat.abstract

import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Item
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

    protected val CONFIG_ITEM_KEY = "BedwarsConfigItem"
    protected val HOLOGRAM_KEY = "BedwarsArmorStand"
    protected val TRACKER_KEY = "BedwarsPlayerTracker"
    protected val DROP_KEY = "BedwarsGenDrop"

    abstract fun <T : LivingEntity> addAI(entity: T)

    abstract fun <T : LivingEntity> removeAI(entity: T)

    abstract fun setConfigItem(itemStack: ItemStack, item: String?): ItemStack

    // custom

    abstract fun getConfigItem(item: ItemStack): String?

    abstract fun isHologram(armorStand: ArmorStand): Boolean

    abstract fun setHologramNBT(armorStand: ArmorStand, holo: Boolean): ArmorStand

    abstract fun setUnbreakable(item: ItemStack, unbreakable: Boolean): ItemStack

    abstract fun setDrop(item: Item, drop: Boolean): Item

    abstract fun isDrop(item: Item): Pair<Boolean, Item>
}