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

package me.dkim19375.bedwars.plugin.manager

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import me.dkim19375.bedwars.plugin.BedwarsPlugin
import org.bukkit.Material
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.lang.reflect.InvocationTargetException

class PacketManager(private val plugin: BedwarsPlugin) {
    fun addListeners() {
        val plugin = plugin
        ProtocolLibrary.getProtocolManager().addPacketListener(object :
            PacketAdapter(plugin, PacketType.Play.Server.REMOVE_ENTITY_EFFECT) {
            override fun onPacketSending(event: PacketEvent) {
                val player = event.packet.getEntityModifier(event.player.world).read(0) as? Player ?: return
                if (event.packet.integers.read(0) != 14) {
                    return
                }
                if (plugin.gameManager.invisPlayers.contains(event.player.uniqueId)) {
                    restoreArmor(player)
                }
            }
        })
        ProtocolLibrary.getProtocolManager().addPacketListener(object :
            PacketAdapter(plugin, PacketType.Play.Server.ENTITY_EQUIPMENT) {
            override fun onPacketSending(event: PacketEvent) {
                val player = event.packet.getEntityModifier(event.player.world).read(0) as? Player ?: return
                if (player.uniqueId == event.player.uniqueId) {
                    return
                }
                if (!plugin.gameManager.invisPlayers.contains(player.uniqueId)) {
                    return
                }
                if (event.packet.integers.read(1) == 0) {
                    return
                }
                val game = plugin.gameManager.getGame(player) ?: return
                if (game.getTeamOfPlayer(player) == game.getTeamOfPlayer(event.player)) {
                    return
                }
                event.packet.itemModifier.write(0, ItemStack(Material.AIR))
            }
        })
    }

    fun collectItem(item: Item, player: Player) {
        val packet = PacketContainer(PacketType.Play.Server.COLLECT)
        packet.integers.write(0, item.entityId)
        packet.integers.write(1, player.entityId)
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet)
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }

    fun hideArmor(player: Player) {
        broadcastNearby(player, setAir(player, 4))
        broadcastNearby(player, setAir(player, 3))
        broadcastNearby(player, setAir(player, 2))
        broadcastNearby(player, setAir(player, 1))
        plugin.gameManager.invisPlayers.add(player.uniqueId)
    }

    fun restoreArmor(player: Player) {
        plugin.gameManager.invisPlayers.remove(player.uniqueId)
        broadcastNearby(player, setItem(player, player.inventory.helmet, 4))
        broadcastNearby(player, setItem(player, player.inventory.chestplate, 3))
        broadcastNearby(player, setItem(player, player.inventory.leggings, 2))
        broadcastNearby(player, setItem(player, player.inventory.boots, 1))
    }

    private fun broadcastNearby(player: Player, packet: PacketContainer) {
        val game = plugin.gameManager.getGame(player) ?: return
        val team = game.getTeamOfPlayer(player) ?: return
        val manager = ProtocolLibrary.getProtocolManager()
        for (observer in manager.getEntityTrackers(player)) {
            if (game.getPlayersInTeam(team).contains(observer.uniqueId)) {
                continue
            }
            try {
                manager.sendServerPacket(observer, packet)
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
        }
    }

    private fun setAir(player: Player, slot: Int): PacketContainer = setItem(player, null, slot)

    private fun setItem(player: Player, item: ItemStack?, slot: Int): PacketContainer {
        val packet = PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT)
        packet.getEntityModifier(player.world).write(0, player)
        packet.integers.write(1, slot)
        packet.itemModifier.write(0, item ?: ItemStack(Material.AIR))
        return packet
    }
}