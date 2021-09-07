package me.dkim19375.bedwars.plugin.config

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.dkimcore.file.YamlFile
import java.io.File

class MainConfigManager(plugin: BedwarsPlugin) :
    YamlFile(MainConfigSettings, File(plugin.dataFolder, "config.yml").path)