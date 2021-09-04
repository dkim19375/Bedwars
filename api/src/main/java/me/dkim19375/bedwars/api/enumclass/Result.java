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

import org.jetbrains.annotations.NotNull;

public enum Result {
    SUCCESS("Successful!"),
    GAME_RUNNING("The game is currently running!"),
    GAME_STOPPED("The game is not running!"),
    NOT_ENOUGH_PLAYERS("Not enough players!"),
    REGENERATING_WORLD("The game world is regenerating!"),
    TOO_MANY_PLAYERS("Too many players!"),
    CANCELLED("Cancelled!");

    @NotNull
    private final String message;

    Result(@NotNull String message) {
        this.message = message;
    }

    @NotNull
    public String getMessage() {
        return message;
    }
}
