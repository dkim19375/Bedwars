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

package me.dkim19375.bedwars.plugin

import com.alessiodp.parties.api.Parties
import com.alessiodp.parties.api.interfaces.PartiesAPI
import com.comphenix.protocol.ProtocolLibrary
import com.onarandombox.MultiverseCore.MultiverseCore
import com.onarandombox.MultiverseCore.api.MVWorldManager
import de.tr7zw.nbtinjector.NBTInjector
import me.dkim19375.bedwars.plugin.command.MainCommand
import me.dkim19375.bedwars.plugin.command.TabCompletionHandler
import me.dkim19375.bedwars.plugin.data.BedData
import me.dkim19375.bedwars.plugin.data.GameData
import me.dkim19375.bedwars.plugin.data.SpawnerData
import me.dkim19375.bedwars.plugin.data.TeamData
import me.dkim19375.bedwars.plugin.listener.*
import me.dkim19375.bedwars.plugin.manager.*
import me.dkim19375.bedwars.plugin.util.logMsg
import me.dkim19375.dkimbukkitcore.config.ConfigFile
import me.dkim19375.dkimbukkitcore.javaplugin.CoreJavaPlugin
import me.dkim19375.itemmovedetectionlib.ItemMoveDetectionLib
import me.tigerhix.lib.scoreboard.ScoreboardLib
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.plugin.Plugin
import kotlin.system.measureTimeMillis

@Suppress("MemberVisibilityCanBePrivate")
class BedwarsPlugin : CoreJavaPlugin() {
    override val defaultConfig: Boolean = false
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
    lateinit var partiesListeners: PartiesListeners
        private set
    var worldManager: MVWorldManager? = null
        private set

    var partiesAPI: PartiesAPI? = null
    var protocolLibSupport = false

    private val serializable = listOf(
        TeamData::class.java,
        BedData::class.java,
        SpawnerData::class.java,
        GameData::class.java
    )

    override fun onLoad() {
        val time = measureTimeMillis {
            serializable.forEach(ConfigurationSerialization::registerClass)
            NBTInjector.inject()
            ScoreboardLib.setPluginInstance(this)
        }
        logMsg("Successfully loaded (not enabled) ${description.name} v${description.version} in ${time}ms!")
    }

    override fun onEnable() {
        val time = measureTimeMillis {
            ItemMoveDetectionLib.register(this)
            initVariables()
            registerCommands()
            registerListeners()
            reloadConfig()
            packetManager.addListeners()
        }
        logMsg("Successfully enabled ${description.name} v${description.version} in ${time}ms!")
    }

    override fun onDisable() {
        gameManager.getGames().values.forEach(BedwarsGame::forceStop)
        gameManager.save()
        if (protocolLibSupport) {
            ProtocolLibrary.getProtocolManager().removePacketListeners(this)
        }
        dataFile.save()
        serializable.reversed().forEach(ConfigurationSerialization::unregisterClass)
        unregisterConfig(dataFile)
    }

    override fun reloadConfig() {
        super.reloadConfig()
        gameManager.reloadData()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Plugin> hookOntoLib(name: String, getPlugin: Boolean = true, api: (T) -> Unit) {
        server.pluginManager.getPlugin(name)?.let {
            if (!getPlugin) {
                api(this as T)
                return
            }
            val plugin = (it as? T) ?: let {
                logMsg("Could not hook into $name!")
                return
            }
            if (plugin.isEnabled) {
                api(plugin)
                logMsg("Hooked onto $name!")
            } else {
                logMsg("Could not hook into $name!")
            }
        } ?: logMsg("Could not hook into $name!")
    }

    private fun initVariables() {
        protocolLibSupport = try {
            Class.forName("com.comphenix.protocol.ProtocolLibrary")
            logMsg("Hooked onto ProtocolLib")
            true
        } catch (e: ClassNotFoundException) {
            logMsg("Could not hook onto ProtocolLib!")
            false
        }
        hookOntoLib("Parties", false) { _: BedwarsPlugin -> partiesAPI = Parties.getApi() }
        hookOntoLib("Multiverse-Core") { pl: MultiverseCore -> worldManager = pl.mvWorldManager }
        dataFile = ConfigFile(this, "data.yml")
        registerConfig(dataFile)
        dataFileManager = DataFileManager(this)
        gameManager = GameManager(this)
        scoreboardManager = ScoreboardManager(this)
        packetManager = PacketManager(this)
        partiesListeners = PartiesListeners(this)
    }

    private fun registerCommands() {
        registerCommand("bedwars", MainCommand(this), TabCompletionHandler(this))
    }

    private fun registerListeners() {
        registerListener(
            PlayerMoveListener(), ExplodeListeners(this), BlockPlaceListener(this),
            BlockBreakListener(this), PlayerQuitListener(this), PlayerDeathListener(this),
            EntityDamageListener(this), ItemTransferListener(this), DamageByOtherListener(this),
            PotionConsumeListener(this), InventoryClickListener(this), PlayerDropItemListener(this),
            PlayerItemDamageListener(this), CommandListeners(this), AsyncPlayerChatListener(this),
            PlayerCoordsChangeListener(this), EntityDamageByEntityListener(this), CraftItemListener(this),
            PlayerInteractListener(this), partiesListeners, scoreboardManager
        )
    }
}