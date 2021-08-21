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
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.lang.reflect.InvocationTargetException

class PacketManager(private val plugin: BedwarsPlugin) {
    fun addListeners() {
        val plugin = plugin
        if (!plugin.protocolLibSupport) {
            return
        }
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
        if (!plugin.protocolLibSupport) {
            return
        }
        broadcastNearby(player, setAir(player, 1))
        broadcastNearby(player, setAir(player, 2))
        broadcastNearby(player, setAir(player, 3))
        broadcastNearby(player, setAir(player, 4))
        plugin.gameManager.invisPlayers.add(player.uniqueId)
    }

    fun restoreArmor(player: Player) {
        if (!plugin.protocolLibSupport) {
            return
        }
        plugin.gameManager.invisPlayers.remove(player.uniqueId)
        broadcastNearby(player, setItem(player, player.inventory.boots, 1))
        broadcastNearby(player, setItem(player, player.inventory.leggings, 2))
        broadcastNearby(player, setItem(player, player.inventory.chestplate, 3))
        broadcastNearby(player, setItem(player, player.inventory.helmet, 4))
    }

    private fun broadcastNearby(player: Player, packet: PacketContainer) {
        if (!plugin.protocolLibSupport) {
            return
        }
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