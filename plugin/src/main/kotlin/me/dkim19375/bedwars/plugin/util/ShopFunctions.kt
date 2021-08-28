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

package me.dkim19375.bedwars.plugin.util

import me.dkim19375.bedwars.plugin.NEW_SOUND
import me.dkim19375.dkimbukkitcore.function.playSound
import org.bukkit.Sound
import org.bukkit.entity.Player

private val BOUGHT_SOUND = if (NEW_SOUND) Sound.valueOf("BLOCK_NOTE_PLING") else Sound.NOTE_PLING

private val ERROR_SOUND = if (NEW_SOUND) Sound.valueOf("BLOCK_ANVIL_LAND") else Sound.ANVIL_LAND

fun Player.playBoughtSound() = playSound(BOUGHT_SOUND, pitch = 7.0f, volume = 0.95f)

fun Player.playErrorSound() = playSound(ERROR_SOUND, pitch = 0.8f, volume = 0.3f)