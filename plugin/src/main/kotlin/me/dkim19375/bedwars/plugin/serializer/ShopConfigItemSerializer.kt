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

package me.dkim19375.bedwars.plugin.serializer

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.data.MainShopConfigItem

class ShopConfigItemSerializer(private val plugin: BedwarsPlugin) : TypeAdapter<MainShopConfigItem>() {
    override fun write(out: JsonWriter, value: MainShopConfigItem?) {
        value ?: return
        out.beginObject()
        out.name("name")
        out.value(value.name)
        out.endObject()
    }

    @Suppress("DuplicatedCode")
    override fun read(input: JsonReader): MainShopConfigItem {
        input.beginObject()
        var name: String? = null
        while (input.hasNext()) {
            if (input.peek() == JsonToken.NAME) {
                if (input.nextName() == "name") {
                    name = input.nextString()
                    break
                }
                input.skipValue()
            }
        }
        input.endObject()
        return name?.let(plugin.shopConfigManager::getItemFromName)
            ?: throw IllegalStateException("Property \"name\" could not be found during deserialization!")
    }
}