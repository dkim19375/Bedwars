package me.dkim19375.bedwars.plugin

import com.comphenix.protocol.ProtocolLibrary
import de.tr7zw.nbtinjector.NBTInjector
import io.github.thatkawaiisam.assemble.Assemble
import me.dkim19375.bedwars.plugin.command.MainCommand
import me.dkim19375.bedwars.plugin.command.TabCompletionHandler
import me.dkim19375.bedwars.plugin.data.BedData
import me.dkim19375.bedwars.plugin.data.GameData
import me.dkim19375.bedwars.plugin.data.SpawnerData
import me.dkim19375.bedwars.plugin.data.TeamData
import me.dkim19375.bedwars.plugin.enumclass.Team
import me.dkim19375.bedwars.plugin.listener.*
import me.dkim19375.bedwars.plugin.manager.*
import me.dkim19375.dkim19375core.ConfigFile
import me.dkim19375.dkim19375core.CoreJavaPlugin
import me.dkim19375.itemmovedetectionlib.ItemMoveDetectionLib
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.event.Listener


@Suppress("MemberVisibilityCanBePrivate")
class BedwarsPlugin : CoreJavaPlugin() {
    lateinit var gameManager: GameManager
        private set
    lateinit var dataFile: ConfigFile
        private set
    lateinit var dataFileManager: DataFileManager
        private set
    lateinit var scoreboardManager: ScoreboardManager
        private set
    lateinit var packetManager: PacketManager
        private set
    lateinit var assemble: Assemble
        private set


    override fun onLoad() {
        val before = System.currentTimeMillis()
        ConfigurationSerialization.registerClass(Team::class.java)
        ConfigurationSerialization.registerClass(TeamData::class.java)
        ConfigurationSerialization.registerClass(BedData::class.java)
        ConfigurationSerialization.registerClass(SpawnerData::class.java)
        ConfigurationSerialization.registerClass(GameData::class.java)
        NBTInjector.inject()
        log("Successfully loaded (not enabled) ${description.name} v${description.version} in ${System.currentTimeMillis() - before}ms!")
    }

    override fun onEnable() {
        val before = System.currentTimeMillis()
        ItemMoveDetectionLib.register()
        initVariables()
        registerCommands()
        registerListeners()
        reloadConfig()
        packetManager.addListeners()
        log("Successfully enabled ${description.name} v${description.version} in ${System.currentTimeMillis() - before}ms!")
    }

    override fun onDisable() {
        gameManager.getGames().values.forEach(BedwarsGame::forceStop)
        gameManager.save()
        ProtocolLibrary.getProtocolManager().removePacketListeners(this)
        dataFile.save()
        ConfigurationSerialization.unregisterClass(Team::class.java)
        ConfigurationSerialization.unregisterClass(TeamData::class.java)
        ConfigurationSerialization.unregisterClass(BedData::class.java)
        ConfigurationSerialization.unregisterClass(SpawnerData::class.java)
        ConfigurationSerialization.unregisterClass(GameData::class.java)
        unregisterConfig(dataFile)
    }

    override fun reloadConfig() {
        super.reloadConfig()
        gameManager.reloadData()
    }

    private fun initVariables() {
        dataFile = ConfigFile(this, "data.yml")
        registerConfig(dataFile)
        dataFileManager = DataFileManager(this)
        gameManager = GameManager(this)
        scoreboardManager = ScoreboardManager(this)
        packetManager = PacketManager(this)
        assemble = Assemble(this, scoreboardManager)
        assemble.ticks = 5
    }

    private fun registerCommands() {
        registerCommand("bedwars", MainCommand(this), TabCompletionHandler(this))
    }

    private fun registerListeners() {
        registerListeners(
            PlayerMoveListener(), ExplodeListeners(this), BlockPlaceListener(this),
            BlockBreakListener(this), PlayerQuitListener(this), PlayerDeathListener(this),
            EntityDamageListener(this), ItemTransferListener(this), DamageByOtherListener(this),
            PotionConsumeListener(this), InventoryClickListener(this), PlayerDropItemListener(this),
            PlayerTeleportListener(this), PlayerInventoryListener(this), PlayerItemDamageListener(this),
            PlayerCoordsChangeListener(this), EntityDamageByEntityListener(this), CraftItemListener(this),
            scoreboardManager
        )
    }

    private fun registerListeners(vararg listeners: Listener) = listeners.forEach(this::registerListener)
}