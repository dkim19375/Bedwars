package me.dkim19375.bedwars

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import me.bristermitten.pdm.SpigotDependencyManager
import me.dkim19375.bedwars.command.MainCommand
import me.dkim19375.bedwars.command.TabCompletionHandler
import me.dkim19375.bedwars.listener.DamageByOtherListener
import me.dkim19375.bedwars.listener.PlayerQuitListener
import me.dkim19375.bedwars.listener.PotionConsumeListener
import me.dkim19375.bedwars.manager.DataFileManager
import me.dkim19375.bedwars.manager.GameManager
import me.dkim19375.dkim19375core.ConfigFile
import me.dkim19375.dkim19375core.CoreJavaPlugin
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.lang.reflect.InvocationTargetException


class BedwarsPlugin : CoreJavaPlugin() {
    lateinit var gameManager: GameManager
        private set
    lateinit var dataFile: ConfigFile
        private set
    lateinit var dataFileManager: DataFileManager
        private set

    override fun onEnable() {
        val before = System.currentTimeMillis()
        SpigotDependencyManager.of(this)
            .loadAllDependencies()
            .join()
        dataFile = ConfigFile(this, "data.yml")
        dataFileManager = DataFileManager(this)
        gameManager = GameManager(this)
        registerCommand("bedwars", MainCommand(this), TabCompletionHandler(this))
        registerListener(PlayerQuitListener(this))
        registerListener(DamageByOtherListener(this))
        registerListener(PotionConsumeListener(this))
        registerPacketListeners()
        log("Done loading ${description.name} v${description.version} in ${System.currentTimeMillis() - before}ms!")
    }

    override fun onDisable() {
        ProtocolLibrary.getProtocolManager().removePacketListeners(this)
    }

    private fun registerPacketListeners() {
        val plugin = this
        ProtocolLibrary.getProtocolManager().addPacketListener(object :
            PacketAdapter(this, PacketType.Play.Server.REMOVE_ENTITY_EFFECT) {
            override fun onPacketSending(event: PacketEvent) {
                val entity: Entity = event.packet.getEntityModifier(event.player.world).read(0) as Entity
                if ((event.packet.integers.read(1) as Int).toInt() == 14 && entity is Player) {
                    if (plugin.gameManager.invisPlayers.contains(entity.uniqueId)) plugin.restoreArmor(entity)
                }
            }
        })
        ProtocolLibrary.getProtocolManager().addPacketListener(object :
            PacketAdapter(this, PacketType.Play.Server.ENTITY_EQUIPMENT) {
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
        gameManager.invisPlayers.add(player.uniqueId)
    }

    fun restoreArmor(player: Player) {
        gameManager.invisPlayers.remove(player.uniqueId)
        broadcastNearby(player, setItem(player, player.inventory.boots, 1))
        broadcastNearby(player, setItem(player, player.inventory.leggings, 2))
        broadcastNearby(player, setItem(player, player.inventory.chestplate, 3))
        broadcastNearby(player, setItem(player, player.inventory.helmet, 4))
    }

    private fun broadcastNearby(player: Player, packet: PacketContainer) {
        val manager = ProtocolLibrary.getProtocolManager()
        for (observer in manager.getEntityTrackers(player)) {
            val gameName = gameManager.getPlayerInGame(observer)?: continue
            val game = gameManager.getGame(gameName)?: continue
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