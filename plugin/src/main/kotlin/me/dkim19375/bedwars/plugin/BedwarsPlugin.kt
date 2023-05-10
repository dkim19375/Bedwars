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
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion
import de.tr7zw.nbtinjector.NBTInjector
import me.dkim19375.bedwars.api.BedwarsAPIProvider
import me.dkim19375.bedwars.plugin.api.BedwarsAPIImpl
import me.dkim19375.bedwars.plugin.builder.GameBuilder
import me.dkim19375.bedwars.plugin.command.MainCommand
import me.dkim19375.bedwars.plugin.command.TabCompletionHandler
import me.dkim19375.bedwars.plugin.config.MainConfigManager
import me.dkim19375.bedwars.plugin.config.ShopConfigManager
import me.dkim19375.bedwars.plugin.data.GameData
import me.dkim19375.bedwars.plugin.data.MainDataFile
import me.dkim19375.bedwars.plugin.data.MainShopConfigItem
import me.dkim19375.bedwars.plugin.listener.*
import me.dkim19375.bedwars.plugin.manager.BedwarsGame
import me.dkim19375.bedwars.plugin.manager.DataFileManager
import me.dkim19375.bedwars.plugin.manager.GameManager
import me.dkim19375.bedwars.plugin.manager.PacketManager
import me.dkim19375.bedwars.plugin.manager.ScoreboardManager
import me.dkim19375.bedwars.plugin.placeholder.BedwarsPAPIExpansion
import me.dkim19375.bedwars.plugin.serializer.LocationSerializer
import me.dkim19375.bedwars.plugin.serializer.ShopConfigItemSerializer
import me.dkim19375.bedwars.plugin.serializer.WorldSerializer
import me.dkim19375.bedwars.plugin.util.initNBTVariables
import me.dkim19375.dkimbukkitcore.config.SpigotConfigFile
import me.dkim19375.dkimbukkitcore.function.logInfo
import me.dkim19375.dkimbukkitcore.javaplugin.CoreJavaPlugin
import me.dkim19375.dkimcore.file.JsonFile
import me.dkim19375.itemmovedetectionlib.ItemMoveDetectionLib
import me.tigerhix.lib.scoreboard.ScoreboardLib
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.plugin.Plugin
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.Properties
import kotlin.io.path.nameWithoutExtension
import kotlin.system.measureTimeMillis

val NEW_SOUND: Boolean = MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_9_R1)
val NEW_MATERIALS: Boolean = MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_13_R1)

@Suppress("MemberVisibilityCanBePrivate")
class BedwarsPlugin : CoreJavaPlugin() {
    val mainConfigManager: MainConfigManager by lazy { MainConfigManager(this) }
    val shopConfigManager = ShopConfigManager(this)
    val shopFile by lazy { SpigotConfigFile(this, dataFolder.resolve("shop.yml")) }
    val gameDataFiles = mutableMapOf<String, JsonFile<GameData>>()
    val mainDataFile: JsonFile<MainDataFile> by lazy {
        JsonFile(
            type = MainDataFile::class,
            file = File(dataFolder, "data/data.json"),
            prettyPrinting = true,
            typeAdapters = jsonSerializers,
            default = { MainDataFile() }
        )
    }
    val gameManager: GameManager by lazy { GameManager(this) }
    val dataFileManager: DataFileManager by lazy { DataFileManager(this) }
    val scoreboardManager: ScoreboardManager by lazy { ScoreboardManager(this) }
    val packetManager: PacketManager by lazy { PacketManager(this) }
    val partiesListeners: PartiesListeners by lazy { PartiesListeners(this) }
    var worldManager: MVWorldManager? = null
        private set
    val mainWorld: String by lazy {
        val properties = Properties()
        val catch = { e: Throwable ->
            e.printStackTrace()
            "world"
        }
        return@lazy try {
            FileInputStream("server.properties").use { stream ->
                properties.load(stream)
                properties.getProperty("level-name", "world")
            }
        } catch (e: IOException) {
            catch(e)
        } catch (e: SecurityException) {
            catch(e)
        } catch (e: IllegalArgumentException) {
            catch(e)
        }
    }

    var partiesAPI: PartiesAPI? = null

    val jsonSerializers by lazy {
        mapOf<Class<*>, Any>(
            Location::class.java to LocationSerializer().nullSafe(),
            MainShopConfigItem::class.java to ShopConfigItemSerializer(this).nullSafe(),
            World::class.java to WorldSerializer().nullSafe()
        )
    }

    override fun onLoad() {
        val time = measureTimeMillis {
            /*logInfo("Loading libraries... (This may take a few seconds up to a minute)")
            logInfo(
                "Finished loading libraries in ${
                    measureTimeMillis {
                        ApplicationBuilder.appending(description.name).build()
                    }
                }ms!"
            )*/
            BedwarsAPIProvider.register(BedwarsAPIImpl(this))
            mainWorld
            initNBTVariables(this)
            NBTInjector.inject()
            ScoreboardLib.setPluginInstance(this)
            BedwarsGame // initialize companion object variables
        }
        logInfo("Successfully loaded (not enabled) ${description.name} v${description.version} in ${time}ms!")
    }

    override fun onEnable() {
        val time = measureTimeMillis {
            ItemMoveDetectionLib.register(this)
            initVariables()
            BedwarsPAPIExpansion(this).register()
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
        ProtocolLibrary.getProtocolManager().removePacketListeners(this)
        unregisterConfig(mainConfigManager)
        unregisterConfig(mainDataFile)
        unregisterConfig(shopFile)
        BedwarsPAPIExpansion(this).unregister()
    }

    override fun reloadConfig() {
        super.reloadConfig()
        shopConfigManager.update()
        for (file in File(dataFolder, "data/games").listFiles() ?: emptyArray()) {
            val name = file.toPath().nameWithoutExtension
            val world = Bukkit.getWorld(name) ?: continue
            val data = GameBuilder(world).build(true) ?: continue
            val newData = JsonFile(
                type = GameData::class,
                file = file,
                prettyPrinting = true,
                typeAdapters = jsonSerializers,
                default = { data }
            )
            gameDataFiles[name] = newData
        }
        gameManager.reloadData()
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
        hookOntoLib("Parties", false) { _: BedwarsPlugin -> partiesAPI = Parties.getApi() }
        hookOntoLib("Multiverse-Core") { pl: MultiverseCore -> worldManager = pl.mvWorldManager }
        registerConfig(mainConfigManager)
        registerConfig(shopFile)
        shopConfigManager.update()
        registerConfig(mainDataFile)
        dataFileManager
        gameManager
        scoreboardManager
        packetManager
        partiesListeners
    }

    private fun registerCommands() {
        registerCommand("bedwars", MainCommand(this), TabCompletionHandler(this))
    }

    private fun registerListeners() {
        registerListener(
            PlayerMoveListener(), EntityExplodeListener(this), BlockPlaceListener(this),
            BlockBreakListener(this), PlayerQuitListener(this), PlayerDeathListener(this),
            EntityDamageListener(this), ItemTransferListener(this), DamageByOtherListener(this),
            PotionConsumeListener(this), InventoryClickListener(this), PlayerPickupItemListener(this),
            WorldInitListener(this), CommandListeners(this), AsyncPlayerChatListener(this),
            PlayerCoordsChangeListener(this), EntityDamageByEntityListener(this), CraftItemListener(this),
            PlayerInteractListener(this), EntityDeathListener(),
            partiesListeners, scoreboardManager
        )
    }
}