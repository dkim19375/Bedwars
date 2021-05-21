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

package me.dkim19375.bedwars.plugin.data

import me.dkim19375.bedwars.plugin.util.clearAll
import me.dkim19375.bedwars.plugin.util.logMsg
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.logging.Level

data class PlayerData(
    val gamemode: GameMode,
    val armor: Array<ItemStack>,
    val inventory: Array<ItemStack>,
    val enderChest: Array<ItemStack>,
    val location: Location,
    val health: Double
) {
    fun apply(player: Player) {
        player.gameMode = gamemode
        player.inventory.armorContents = armor
        player.inventory.contents = inventory
        player.enderChest.contents = enderChest
        player.teleport(location)
        player.activePotionEffects.forEach { e -> player.removePotionEffect(e.type) }
        player.health = health
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlayerData

        if (gamemode != other.gamemode) return false
        if (!armor.contentEquals(other.armor)) return false
        if (!inventory.contentEquals(other.inventory)) return false
        if (!enderChest.contentEquals(other.enderChest)) return false
        if (location != other.location) return false
        if (health != other.health) return false

        return true
    }

    override fun hashCode(): Int {
        var result = gamemode.hashCode()
        result = 31 * result + armor.contentHashCode()
        result = 31 * result + inventory.contentHashCode()
        result = 31 * result + enderChest.contentHashCode()
        result = 31 * result + location.hashCode()
        result = 31 * result + health.hashCode()
        return result
    }

    override fun toString(): String {
        return "PlayerData(" +
                "gamemode=$gamemode, " +
                "armor=${armor.contentToString()}, " +
                "inventory=${inventory.contentToString()}, " +
                "enderChest=${enderChest.contentToString()}, " +
                "location=$location, " +
                "health=$health)"
    }


    companion object {
        @Suppress("MemberVisibilityCanBePrivate")
        fun createData(player: Player, default: Location?) =
            PlayerData(
                player.gameMode,
                player.inventory.armorContents,
                player.inventory.contents,
                player.enderChest.contents,
                default ?: player.location,
                player.health
            )

        fun createDataAndReset(player: Player, default: Location?, location: Location?, gamemode: GameMode = GameMode.SURVIVAL): PlayerData {
            val data = createData(player, default)
            player.gameMode = gamemode
            player.inventory.clearAll()
            player.enderChest.clear()
            for (effect in player.activePotionEffects) {
                player.removePotionEffect(effect.type)
            }
            player.health = 20.0
            if (location != null) {
                if (!player.teleport(location)) {
                    logMsg("Could not teleport player ${player.name} successfully!", Level.SEVERE)
                }
            }
            return data
        }
    }
}
