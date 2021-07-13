package me.dkim19375.bedwars.plugin.util

import org.bukkit.Sound
import org.bukkit.entity.Player

fun Player.playBoughtSound() = playSound(Sound.NOTE_PLING, pitch = 7.0f, volume = 0.95f)

fun Player.playErrorSound() = playSound(Sound.ANVIL_LAND, pitch = 0.8f, volume = 0.6f)