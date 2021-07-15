/*
 * MIT License
 *
 * Copyright (c) 2021 dkim19375
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
                valueOf(str.toUpperCase())
            } catch (_: IllegalArgumentException) {
                return null
            }
        }
    }
}