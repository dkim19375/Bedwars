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

package me.dkim19375.bedwars.api.event.player;

import me.dkim19375.bedwars.api.BedwarsGameAPI;
import me.dkim19375.bedwars.api.event.BedwarsEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public abstract class BedwarsPlayerEvent extends PlayerEvent implements BedwarsEvent {

    @NotNull
    private final BedwarsGameAPI game;

    public BedwarsPlayerEvent(@NotNull Player player, @NotNull BedwarsGameAPI game) {
        super(player);
        this.game = game;
    }

    @NotNull
    @Override
    @Contract(pure = true)
    public BedwarsGameAPI getGame() {
        return game;
    }
}
