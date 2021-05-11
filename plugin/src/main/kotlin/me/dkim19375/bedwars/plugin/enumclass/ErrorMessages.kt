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