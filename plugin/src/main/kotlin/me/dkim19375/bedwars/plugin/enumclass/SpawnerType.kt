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

import me.dkim19375.bedwars.plugin.util.Delay
import org.bukkit.Material

@Suppress("MemberVisibilityCanBePrivate")
enum class SpawnerType(val material: Material, val maxAmount: Int, val delayFirst: Delay, val delaySecond: Delay = delayFirst, val delayThird: Delay = delaySecond) {
    IRON(Material.IRON_INGOT, 48, Delay.fromMillis(1800)),
    GOLD(Material.GOLD_INGOT, 12, Delay.fromMillis(7500)),
    DIAMOND(Material.DIAMOND, 4, Delay.fromSeconds(30), Delay.fromSeconds(23), Delay.fromSeconds(18)),
    EMERALD(Material.EMERALD, 2, Delay.fromSeconds(65), Delay.fromSeconds(50), Delay.fromSeconds(40));

    fun getDelay(delay: Int) = when (delay) {
        1 -> delayFirst
        2 -> delaySecond
        else -> delayThird
    }

    companion object {
        fun fromString(str: String?): SpawnerType? {
            str ?: return null
            return try {
                valueOf(str.uppercase())
            } catch (_: IllegalArgumentException) {
                return null
            }
        }
    }
}