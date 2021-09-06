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

import me.dkim19375.bedwars.api.BedwarsAPI;
import me.dkim19375.dkimcore.annotation.API;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Team {
    RED(DyeColor.RED, ChatColor.RED, "Red"),
    BLUE(DyeColor.LIGHT_BLUE, ChatColor.BLUE, "Blue"),
    GREEN(DyeColor.LIME, ChatColor.GREEN, "Green"),
    YELLOW(DyeColor.YELLOW, ChatColor.YELLOW, "Yellow"),
    AQUA(DyeColor.CYAN, ChatColor.AQUA, "Aqua"),
    WHITE(DyeColor.WHITE, ChatColor.WHITE, "White"),
    PINK(DyeColor.PINK, ChatColor.LIGHT_PURPLE, "Pink"),
    GRAY(DyeColor.GRAY, ChatColor.GRAY, "Gray");

    @NotNull
    private final DyeColor color;
    @NotNull
    private final ChatColor chatColor;
    @NotNull
    private final String displayName;

    Team(@NotNull DyeColor color, @NotNull ChatColor chatColor, @NotNull String displayName) {
        this.color = color;
        this.chatColor = chatColor;
        this.displayName = displayName;
    }

    @NotNull
    @Contract(pure = true)
    public DyeColor getColor() {
        return color;
    }

    @NotNull
    @Contract(pure = true)
    public ChatColor getChatColor() {
        return chatColor;
    }

    @NotNull
    @Contract(pure = true)
    public String getDisplayName() {
        return displayName;
    }

    @Nullable
    public static Team fromString(@Nullable String str) {
        if (str == null) {
            return null;
        }
        try {
            return valueOf(str.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    @API
    @NotNull
    public ItemStack getColored(@NotNull Material material, @NotNull BedwarsAPI api) {
        return getColored(new ItemStack(material), api);
    }

    @API
    @NotNull
    public ItemStack getColored(@NotNull ItemStack item, @NotNull BedwarsAPI api) {
        return api.getColored(this, item);
    }
}