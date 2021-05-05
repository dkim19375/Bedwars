package me.dkim19375.bedwars.plugin.data

import me.dkim19375.bedwars.plugin.util.update
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

data class PlayerData(
    val gamemode: GameMode,
    val armor: Array<ItemStack>,
    val inventory: Array<ItemStack>,
    val enderChest: Array<ItemStack>,
    val location: Location
) {
    fun apply(player: Player) {
        player.gameMode = gamemode
        player.inventory.armorContents = armor
        player.inventory.contents = inventory
        player.enderChest.contents = enderChest
        player.teleport(location.update())
        player.activePotionEffects.forEach { e -> player.removePotionEffect(e.type) }
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

        return true
    }

    override fun hashCode(): Int {
        var result = gamemode.hashCode()
        result = 31 * result + armor.contentHashCode()
        result = 31 * result + inventory.contentHashCode()
        result = 31 * result + enderChest.contentHashCode()
        result = 31 * result + location.hashCode()
        return result
    }

    override fun toString(): String {
        return "PlayerData(" +
                "gamemode=$gamemode, " +
                "armor=${armor.contentToString()}, " +
                "inventory=${inventory.contentToString()}, " +
                "enderChest=${enderChest.contentToString()}, " +
                "location=$location)"
    }


    companion object {
        @Suppress("MemberVisibilityCanBePrivate")
        fun getPlayer(player: Player, default: Location?) =
            PlayerData(
                player.gameMode,
                player.inventory.armorContents,
                player.inventory.contents,
                player.enderChest.contents,
                default ?: player.location
            )

        fun getPlayerAndReset(player: Player, default: Location?, location: Location?, gamemode: GameMode = GameMode.SURVIVAL): PlayerData {
            val data = getPlayer(player, default)
            player.gameMode = gamemode
            player.inventory.clear()
            player.enderChest.clear()
            if (location != null) {
                player.teleport(location.update())
            }
            return data
        }
    }
}
