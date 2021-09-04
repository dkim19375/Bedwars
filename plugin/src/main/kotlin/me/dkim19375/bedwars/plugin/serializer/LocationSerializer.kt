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

import com.google.gson.*
import org.bukkit.Bukkit
import org.bukkit.Location
import java.lang.reflect.Type

class LocationSerializer : JsonSerializer<Location>,
    JsonDeserializer<Location> {
    override fun serialize(
        src: Location,
        typeOfSrc: Type,
        context: JsonSerializationContext,
    ): JsonElement {
        val obj = JsonObject()
        obj.addProperty("world", src.world.name)
        obj.addProperty("x", src.x)
        obj.addProperty("y", src.y)
        obj.addProperty("z", src.z)
        obj.addProperty("yaw", src.yaw)
        obj.addProperty("pitch", src.pitch)
        return obj
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext,
    ): Location {
        val obj = json.asJsonObject
        val world = Bukkit.getWorld(obj.get("world").asString) ?: throw IllegalArgumentException("unknown world")
        return Location(world,
            obj.getAsJsonPrimitive("x").asDouble,
            obj.getAsJsonPrimitive("y").asDouble,
            obj.getAsJsonPrimitive("z").asDouble,
            obj.getAsJsonPrimitive("yaw").asFloat,
            obj.getAsJsonPrimitive("pitch").asFloat)
    }
}