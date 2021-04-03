package me.dkim19375.bedwars.command

import com.google.common.collect.HashMultimap
import com.google.common.collect.Lists
import me.dkim19375.bedwars.BedwarsPlugin
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.util.StringUtil

class TabCompletionHandler(private val plugin: BedwarsPlugin) : TabCompleter {

    private val completesListMap: HashMultimap<String, String> = HashMultimap.create()

    init {
        add("core", "help", "well", "reload")
    }

    private fun add(key: String, vararg args: String) {
        completesListMap.putAll(key, listOf(*args))
    }

    private fun getPartial(token: String, collection: Iterable<String>): List<String>? {
        return StringUtil.copyPartialMatches(token, collection, ArrayList())
    }

    override fun onTabComplete(sender: CommandSender, cmd: Command, label: String, args: Array<String>): List<String>? {
        return when (args.size) {
            0 -> Lists.newArrayList(completesListMap["core"])
            1 -> getPartial(args[0], completesListMap["core"])
            2 -> {
                if (args[0].equals("well", ignoreCase = true)) {
                    getPartial(args[1], completesListMap["well"])
                } else listOf()
            }
            else -> listOf()
        }
    }
}