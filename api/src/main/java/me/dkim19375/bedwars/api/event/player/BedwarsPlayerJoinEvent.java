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
import me.dkim19375.dkimcore.annotation.API;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class BedwarsPlayerJoinEvent extends BedwarsPlayerEvent implements Cancellable {
    @NotNull
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled = false;

    public BedwarsPlayerJoinEvent(@NotNull Player player, @NotNull BedwarsGameAPI game) {
        super(player, game);
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @API
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    @Contract(pure = true)
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
