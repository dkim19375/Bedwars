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

package me.dkim19375.bedwars.plugin.enumclass

import org.bukkit.Bukkit
import org.bukkit.ChatColor

enum class ErrorMessages(val message: String) {
    NO_PERMISSION(
        Bukkit.getPluginCommand("bedwars")?.permissionMessage
            ?: "${ChatColor.RED}You do not have permission to run this command!"
    ),
    TOO_LITTLE_ARGS("${ChatColor.RED}Not enough arguments!"),
    INVALID_GAME("${ChatColor.RED}That is not a valid game!"),
    INVALID_ARG("${ChatColor.RED}Invalid argument!"),
    MUST_BE_PLAYER("${ChatColor.RED}You must be a player!"),
    NOT_EDIT_MODE("${ChatColor.RED}The game must be in edit mode!"),
    INVALID_WORLD("${ChatColor.RED}The world is not valid!"),
    INVALID_TEAM("${ChatColor.RED}That is not a valid team!"),
    GAME_ALREADY_EXISTS("${ChatColor.RED}That game already exists!"),
}