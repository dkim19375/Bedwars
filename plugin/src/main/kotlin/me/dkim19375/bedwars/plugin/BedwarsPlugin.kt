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

package me.dkim19375.bedwars.plugin

import com.alessiodp.parties.api.Parties
import com.alessiodp.parties.api.interfaces.PartiesAPI
import com.comphenix.protocol.ProtocolLibrary
import com.onarandombox.MultiverseCore.MultiverseCore
import com.onarandombox.MultiverseCore.api.MVWorldManager
import de.tr7zw.nbtinjector.NBTInjector
import io.github.slimjar.app.builder.ApplicationBuilder
import me.dkim19375.bedwars.plugin.command.MainCommand
import me.dkim19375.bedwars.plugin.command.TabCompletionHandler
import me.dkim19375.bedwars.plugin.config.ConfigManager
import me.dkim19375.bedwars.plugin.data.BedData
import me.dkim19375.bedwars.plugin.data.GameData
import me.dkim19375.bedwars.plugin.data.SpawnerData
import me.dkim19375.bedwars.plugin.data.TeamData
import me.dkim19375.bedwars.plugin.listener.*
import me.dkim19375.bedwars.plugin.manager.*
import me.dkim19375.bedwars.plugin.util.initNBTVariables
import me.dkim19375.dkimbukkitcore.config.ConfigFile
import me.dkim19375.dkimbukkitcore.function.logInfo
import me.dkim19375.dkimbukkitcore.javaplugin.CoreJavaPlugin
import me.dkim19375.itemmovedetectionlib.ItemMoveDetectionLib
import me.tigerhix.lib.scoreboard.ScoreboardLib
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.plugin.Plugin
import java.io.FileInputStream
import java.io.IOException
import java.util.*
import kotlin.system.measureTimeMillis

@Suppress("MemberVisibilityCanBePrivate")
class BedwarsPlugin : CoreJavaPlugin() {
    val configManager = ConfigManager(this)
    val shopFile = ConfigFile(this, "shop.yml")
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
    lateinit var mainWorld: String
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
            logInfo("Loading libraries... (This may take a few seconds up to a minute)")
            logInfo(
                "Finished loading libraries in ${
                    measureTimeMillis {
                        ApplicationBuilder.appending(description.name).build()
                    }
                }ms!"
            )
            val properties = Properties()
            val catch = { e: Throwable ->
                e.printStackTrace()
                mainWorld = "world"
            }
            try {
                FileInputStream("server.properties").use { stream ->
                    properties.load(stream)
                    mainWorld = properties.getProperty("level-name", "world")
                }
            } catch (e: IOException) {
                catch(e)
            } catch (e: SecurityException) {
                catch(e)
            } catch (e: IllegalArgumentException) {
                catch(e)
            }
            initNBTVariables(this)
            serializable.forEach(ConfigurationSerialization::registerClass)
            NBTInjector.inject()
            ScoreboardLib.setPluginInstance(this)
        }
        logInfo("Successfully loaded (not enabled) ${description.name} v${description.version} in ${time}ms!")
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
        logInfo("Successfully enabled ${description.name} v${description.version} in ${time}ms!")
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
        unregisterConfig(shopFile)
    }

    override fun reloadConfig() {
        super.reloadConfig()
        gameManager.reloadData()
        configManager.update()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Plugin> hookOntoLib(name: String, getPlugin: Boolean = true, api: (T) -> Unit) =
        server.pluginManager.getPlugin(name)?.let {
            if (!getPlugin) {
                api(this as T)
                return
            }
            val plugin = (it as? T) ?: let {
                logInfo("Could not hook into $name!")
                return
            }
            if (plugin.isEnabled) {
                api(plugin)
                logInfo("Hooked onto $name!")
                return@let
            }
            logInfo("Could not hook into $name!")
        } ?: logInfo("Could not hook into $name!")

    private fun initVariables() {
        hookOntoLib("ProtocolLib", false) { _: BedwarsPlugin -> protocolLibSupport = true }
        hookOntoLib("Parties", false) { _: BedwarsPlugin -> partiesAPI = Parties.getApi() }
        hookOntoLib("Multiverse-Core") { pl: MultiverseCore -> worldManager = pl.mvWorldManager }
        dataFile = ConfigFile(this, "data.yml")
        registerConfig(dataFile)
        registerConfig(shopFile)
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
            PlayerInteractListener(this), ProjectileLaunchListener(this), PlayerPickupItemListener(this),
            WorldInitListener(this), partiesListeners, scoreboardManager
        )
    }
}