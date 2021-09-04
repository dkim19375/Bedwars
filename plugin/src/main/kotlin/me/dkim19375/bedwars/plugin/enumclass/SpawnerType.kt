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

package me.dkim19375.bedwars.plugin.enumclass

import me.dkim19375.bedwars.api.enumclass.SpawnerTypes
import me.dkim19375.bedwars.plugin.util.Delay
import me.dkim19375.dkimcore.extension.runCatchingOrNull
import org.bukkit.Material

@Suppress("MemberVisibilityCanBePrivate")
enum class SpawnerType(
    val type: SpawnerTypes,
    val material: Material,
    val maxAmount: Int,
    val delayFirst: Delay,
    val delaySecond: Delay = delayFirst,
    val delayThird: Delay = delaySecond,
    val secondTime: Delay? = null,
    val thirdTime: Delay? = null
) {
    IRON(SpawnerTypes.IRON, Material.IRON_INGOT, 48, Delay.fromMillis(1800)),
    GOLD(SpawnerTypes.GOLD, Material.GOLD_INGOT, 12, Delay.fromMillis(7500)),
    DIAMOND(
        type = SpawnerTypes.DIAMOND,
        material = Material.DIAMOND,
        maxAmount = 4,
        delayFirst = Delay.fromSeconds(30),
        delaySecond = Delay.fromSeconds(23),
        delayThird = Delay.fromSeconds(12),
        secondTime = Delay.fromMinutes(5),
        thirdTime = Delay.fromMinutes(17),
    ),
    EMERALD(
        type = SpawnerTypes.EMERALD,
        material = Material.EMERALD,
        maxAmount = 2,
        delayFirst = Delay.fromMinutes(1),
        delaySecond = Delay.fromSeconds(45),
        delayThird = Delay.fromSeconds(30),
        secondTime = Delay.fromMinutes(11),
        thirdTime = Delay.fromMinutes(23)
    );

    fun getDelay(delay: Int) = when (delay) {
        1 -> delayFirst
        2 -> delaySecond
        else -> delayThird
    }

    companion object {
        fun fromAPIType(type: SpawnerTypes?): SpawnerType? {
            type ?: return null
            return values().firstOrNull { it.type == type }
        }

        fun fromString(str: String?): SpawnerType? {
            str ?: return null
            return runCatchingOrNull { valueOf(str.uppercase()) }
        }

        fun fromMaterial(mat: Material?): SpawnerType? {
            mat ?: return null
            return values().firstOrNull { it.material == mat }
        }
    }
}