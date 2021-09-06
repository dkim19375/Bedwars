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

package me.dkim19375.bedwars.plugin.api

import me.dkim19375.bedwars.api.BedwarsAPI
import me.dkim19375.bedwars.api.BedwarsGameAPI
import me.dkim19375.bedwars.api.data.BedwarsGameData
import me.dkim19375.bedwars.api.data.BedwarsStatisticsData
import me.dkim19375.bedwars.api.enumclass.Team
import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.data.GameData
import me.dkim19375.bedwars.plugin.manager.GameManager
import me.dkim19375.bedwars.plugin.util.getColored
import me.dkim19375.dkimcore.extension.toImmutableMap
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

class BedwarsAPIImpl(private val plugin: BedwarsPlugin) : BedwarsAPI {
    private val gameManager: GameManager
        get() = plugin.gameManager

    override fun getGames(): Map<String, BedwarsGameAPI> = gameManager.getGames().map {
        it.key to it.value.getAPI()
    }.toImmutableMap()

    override fun getGame(name: String?): BedwarsGameAPI? = gameManager.getGame(name)?.getAPI()

    override fun deleteGame(game: BedwarsGameAPI) = gameManager.deleteGame(game.gameData as GameData)

    override fun saveGameData(data: BedwarsGameData) = (data as GameData).save(plugin)

    override fun getColored(team: Team, item: ItemStack): ItemStack = team.getColored(item)

    override fun getStatistics(player: Player): BedwarsStatisticsData = getStatistics(player.uniqueId)

    override fun getStatistics(player: UUID): BedwarsStatisticsData {
        return plugin.mainDataFile.get().getStatistics(player)
    }
}