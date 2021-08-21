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

package me.dkim19375.bedwars.plugin.data

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.util.*
import me.dkim19375.dkimbukkitcore.data.toWrapper
import me.dkim19375.dkimbukkitcore.function.logInfo
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

data class PlayerData(
    val gamemode: GameMode,
    val armor: Array<ItemStack>,
    val inventory: List<ItemStack?>,
    val enderChest: Array<ItemStack>,
    val location: Location,
    val health: Double
) {
    fun apply(player: Player) {
        player.compassTarget = player.world.spawnLocation.clone()
        player.gameMode = gamemode
        player.inventory.setAllContents(inventory)
        player.inventory.armorContents = armor
        player.enderChest.contents = enderChest
        player.teleportUpdated(location)
        player.compassTarget = player.world.spawnLocation.clone()
        player.activePotionEffects.forEach { e -> player.removePotionEffect(e.type) }
        player.health = health
    }

    override fun hashCode(): Int {
        var result = gamemode.hashCode()
        result = 31 * result + armor.contentHashCode()
        result = 31 * result + inventory.hashCode()
        result = 31 * result + enderChest.contentHashCode()
        result = 31 * result + location.hashCode()
        result = 31 * result + health.hashCode()
        return result
    }

    override fun toString(): String {
        return "PlayerData(" +
                "gamemode=$gamemode, " +
                "armor=${armor.contentToString()}, " +
                "inventory=${inventory}, " +
                "enderChest=${enderChest.contentToString()}, " +
                "location=$location, " +
                "health=$health)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlayerData

        if (gamemode != other.gamemode) return false
        if (!armor.contentEquals(other.armor)) return false
        if (inventory != other.inventory) return false
        if (!enderChest.contentEquals(other.enderChest)) return false
        if (location != other.location) return false
        if (health != other.health) return false

        return true
    }


    companion object {
        @Suppress("MemberVisibilityCanBePrivate")
        fun createData(player: Player, default: Location?) =
            PlayerData(
                player.gameMode,
                player.inventory.armorContents,
                player.inventory.getAllContents(),
                player.enderChest.contents,
                default ?: player.location,
                player.health
            )

        fun createDataAndReset(
            player: Player,
            default: Location?,
            location: Location?,
            gamemode: GameMode = GameMode.SURVIVAL
        ): PlayerData {
            val data = createData(player, default)
            player.gameMode = gamemode
            player.inventory.clearAll()
            player.enderChest.clear()
            for (effect in player.activePotionEffects) {
                player.removePotionEffect(effect.type)
            }
            player.health = 20.0
            val newLoc = location?.update()
            if (newLoc != null) {
                val plugin = JavaPlugin.getPlugin(BedwarsPlugin::class.java)
                plugin.worldManager?.getMVWorld(newLoc.world)?.setKeepSpawnInMemory(true)
                if (!newLoc.chunk.load(true)) {
                    logInfo("Could not load chunk: ${newLoc.toWrapper().format()}!", Level.SEVERE)
                }
                if (!player.teleportUpdated(newLoc)) {
                    logInfo("Could not teleport player ${player.name} successfully!", Level.SEVERE)
                }
            }
            return data
        }
    }
}