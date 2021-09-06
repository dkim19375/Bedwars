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

package me.dkim19375.bedwars.api.data;

import me.dkim19375.dkimcore.annotation.API;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface BedwarsPlayerData {

    @API
    @NotNull
    @Contract(pure = true)
    GameMode getPlayerGamemode();

    @API
    @NotNull
    @Contract(pure = true)
    ItemStack[] getPlayerArmor();

    @API
    @NotNull
    @Contract(pure = true)
    List<@Nullable ItemStack> getPlayerInventory();

    @API
    @NotNull
    ItemStack[] getPlayerEnderChest();

    @API
    @NotNull
    @Contract(pure = true)
    Location getPlayerLocation();

    @API
    @Contract(pure = true)
    double getPlayerHealth();

    @API
    void apply(@NotNull Player player);

    @API
    @NotNull
    BedwarsPlayerData clone(
            @Nullable GameMode gamemode,
            @NotNull ItemStack @Nullable[] armor,
            @Nullable List<@Nullable ItemStack> inventory,
            @NotNull ItemStack @Nullable[] enderChest,
            @Nullable Location location,
            @Nullable Double health
    );
}
