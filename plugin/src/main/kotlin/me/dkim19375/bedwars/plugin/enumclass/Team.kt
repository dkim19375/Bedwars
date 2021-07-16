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

import me.dkim19375.bedwars.plugin.util.getWrapper
import org.bukkit.ChatColor
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

enum class Team(val color: DyeColor, val chatColor: ChatColor, val displayName: String) {
    RED(DyeColor.RED, ChatColor.RED, "Red"),
    YELLOW(DyeColor.YELLOW, ChatColor.YELLOW, "Yellow"),
    GREEN(DyeColor.LIME, ChatColor.GREEN, "Green"),
    BLUE(DyeColor.LIGHT_BLUE, ChatColor.BLUE, "Blue");

    companion object {
        fun fromString(str: String?): Team? {
            str ?: return null
            return try {
                valueOf(str.uppercase())
            } catch (_: IllegalArgumentException) {
                return null
            }
        }
    }
}

@Suppress("unused")
fun Team.getColored(material: Material): ItemStack = getColored(ItemStack(material))

fun Team.getColored(item: ItemStack): ItemStack {
    val wrapper = item.getWrapper()
    return wrapper.toItemStack(color)
}