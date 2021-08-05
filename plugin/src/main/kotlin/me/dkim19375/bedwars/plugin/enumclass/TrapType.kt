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