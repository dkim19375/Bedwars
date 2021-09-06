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

package me.dkim19375.bedwars.api.enumclass;

import me.dkim19375.bedwars.api.BedwarsGameAPI;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the current {@link GameState} of the {@link BedwarsGameAPI}
 */
public enum GameState {
    STOPPED("Stopped", ChatColor.RED),
    LOBBY("Ready", ChatColor.GREEN),
    STARTING("Starting", ChatColor.YELLOW),
    STARTED("Running", ChatColor.YELLOW),
    REGENERATING_WORLD("Restarting", ChatColor.RED),
    GAME_END("Game Ended", ChatColor.RED);

    @NotNull
    private final String displayname;
    @NotNull
    private final ChatColor color;

    GameState(@NotNull String displayname, @NotNull ChatColor color) {
        this.displayname = displayname;
        this.color = color;
    }

    @NotNull
    @Contract(pure = true)
    public String getDisplayname() {
        return displayname;
    }

    @NotNull
    @Contract(pure = true)
    public ChatColor getColor() {
        return color;
    }
}
