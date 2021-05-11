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

import org.bukkit.Material
import org.bukkit.potion.PotionEffectType

enum class TrapType(
    val displayName: String,
    val duration: Int = 0,
    val material: Material,
    val removeEffects: Set<PotionEffectType> = setOf(),
    val effects: Map<PotionEffectType, Int> = emptyMap()
) {
    ITS_A_TRAP(
        "Its a trap!",
        8 * 20,
        Material.TRIPWIRE_HOOK,
        effects = mapOf(Pair(PotionEffectType.BLINDNESS, 1), Pair(PotionEffectType.SLOW, 1))
    ),
    COUNTER_OFFENSIVE(
        "Counter Offensive",
        10 * 20,
        Material.FEATHER,
        effects = mapOf(Pair(PotionEffectType.SPEED, 1), Pair(PotionEffectType.JUMP, 2))
    ),
    ALARM(
        "Alarm",
        material = Material.REDSTONE_TORCH_ON,
        removeEffects = setOf(PotionEffectType.INVISIBILITY)
    ),
    MINER_FATIGUE(
        "Mining Fatigue",
        10 * 20,
        Material.IRON_PICKAXE,
        effects = mapOf(Pair(PotionEffectType.SLOW_DIGGING, 1))
    )
}