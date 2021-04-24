package me.dkim19375.bedwars.plugin.data

import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

data class PlayerData(
    val gamemode: GameMode,
    val inventory: Array<ItemStack>,
    val enderChest: Array<ItemStack>,
    val location: Location
) {
    fun apply(player: Player) {
        player.gameMode = gamemode
        player.inventory.contents = inventory
        player.enderChest.contents = enderChest
        player.teleport(location)
        player.activePotionEffects.forEach { e -> player.removePotionEffect(e.type) }
    }

    companion object {
        @Suppress("MemberVisibilityCanBePrivate")
        fun getPlayer(player: Player) =
            PlayerData(player.gameMode, player.inventory.contents, player.enderChest.contents, player.location)

        fun getPlayerAndReset(player: Player, location: Location?, gamemode: GameMode = GameMode.SURVIVAL): PlayerData {
            val data = getPlayer(player)
            player.gameMode = gamemode
            player.inventory.clear()
            player.enderChest.clear()
            if (location != null) {
                player.teleport(location)
            }
            return data
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlayerData

        if (gamemode != other.gamemode) return false
        if (!inventory.contentEquals(other.inventory)) return false
        if (!enderChest.contentEquals(other.enderChest)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = gamemode.hashCode()
        result = 31 * result + inventory.contentHashCode()
        result = 31 * result + enderChest.contentHashCode()
        return result
    }
}
