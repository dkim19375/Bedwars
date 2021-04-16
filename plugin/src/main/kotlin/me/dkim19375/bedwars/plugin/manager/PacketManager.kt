package me.dkim19375.bedwars.plugin.manager

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import me.dkim19375.bedwars.plugin.BedwarsPlugin
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.lang.reflect.InvocationTargetException

class PacketManager(private val plugin: BedwarsPlugin) {
    fun addListeners() {
        val plugin = plugin
        ProtocolLibrary.getProtocolManager().addPacketListener(object :
            PacketAdapter(plugin, PacketType.Play.Server.REMOVE_ENTITY_EFFECT) {
            override fun onPacketSending(event: PacketEvent) {
                val entity: Entity = event.packet.getEntityModifier(event.player.world).read(0) as Entity
                if ((event.packet.integers.read(1) as Int).toInt() == 14 && entity is Player) {
                    if (plugin.gameManager.invisPlayers.contains(entity.uniqueId)) restoreArmor(entity)
                }
            }
        })
        ProtocolLibrary.getProtocolManager().addPacketListener(object :
            PacketAdapter(plugin, PacketType.Play.Server.ENTITY_EQUIPMENT) {
            override fun onPacketSending(event: PacketEvent) {
                val entity = event.packet.getEntityModifier(event.player.world).read(0) as Entity
                if (entity is Player) {
                    if (entity != event.player && plugin.gameManager.invisPlayers.contains(entity.uniqueId) && (event.packet.integers.read(
                            1
                        ) as Int).toInt() != 0
                    ) event.isCancelled = true
                }
            }
        })
    }

    fun hideArmor(player: Player) {
        broadcastNearby(player, setAir(player, 1))
        broadcastNearby(player, setAir(player, 2))
        broadcastNearby(player, setAir(player, 3))
        broadcastNearby(player, setAir(player, 4))
        plugin.gameManager.invisPlayers.add(player.uniqueId)
    }

    fun restoreArmor(player: Player) {
        plugin.gameManager.invisPlayers.remove(player.uniqueId)
        broadcastNearby(player, setItem(player, player.inventory.boots, 1))
        broadcastNearby(player, setItem(player, player.inventory.leggings, 2))
        broadcastNearby(player, setItem(player, player.inventory.chestplate, 3))
        broadcastNearby(player, setItem(player, player.inventory.helmet, 4))
    }

    private fun broadcastNearby(player: Player, packet: PacketContainer) {
        val manager = ProtocolLibrary.getProtocolManager()
        for (observer in manager.getEntityTrackers(player)) {
            val gameName = plugin.gameManager.getPlayerInGame(observer)?: continue
            val game = plugin.gameManager.getGame(gameName)?: continue
            val team = game.getTeamOfPlayer(player)?: continue
            if (game.getPlayersInTeam(team).contains(observer.uniqueId)) {
                continue
            }
            try {
                manager.sendServerPacket(observer, packet)
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
                throw RuntimeException("Cannot send packet.", e)
            }
        }
    }

    private fun setAir(player: Player, slot: Int): PacketContainer {
        val packet = PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT)
        packet.getEntityModifier(player.world).write(0, player)
        packet.integers.write(1, Integer.valueOf(slot))
        packet.itemModifier.write(0, ItemStack(Material.AIR))
        return packet
    }

    private fun setItem(player: Player, item: ItemStack, slot: Int): PacketContainer {
        val packet = PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT)
        packet.getEntityModifier(player.world).write(0, player)
        packet.integers.write(1, Integer.valueOf(slot))
        packet.itemModifier.write(0, item)
        return packet
    }
}