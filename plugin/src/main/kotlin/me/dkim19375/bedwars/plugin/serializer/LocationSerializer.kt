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
import org.bukkit.Bukkit
import org.bukkit.Location

class LocationSerializer : TypeAdapter<Location>() {
    override fun write(out: JsonWriter, value: Location?) {
        value ?: return
        out.beginObject()
        out.name("world")
        out.value(value.world.name)
        out.name("x")
        out.value(value.x)
        out.name("y")
        out.value(value.y)
        out.name("z")
        out.value(value.z)
        out.name("yaw")
        out.value(value.yaw)
        out.name("pitch")
        out.value(value.pitch)
        out.endObject()
    }

    override fun read(input: JsonReader): Location {
        input.beginObject()
        var world: String? = null
        var x: Double? = null
        var y: Double? = null
        var z: Double? = null
        var yaw: Float? = null
        var pitch: Float? = null
        var fieldName: String? = null
        @Suppress("UNUSED_VALUE") // idk im just following the tutorial
        while (input.hasNext()) {
            var token = input.peek()
            if (token == JsonToken.NAME) {
                fieldName = input.nextName()
            }
            if (fieldName == "world") {
                token = input.peek()
                world = input.nextString()
                continue
            }
            if (fieldName == "x") {
                token = input.peek()
                x = input.nextDouble()
                continue
            }
            if (fieldName == "y") {
                token = input.peek()
                y = input.nextDouble()
                continue
            }
            if (fieldName == "z") {
                token = input.peek()
                z = input.nextDouble()
                continue
            }
            if (fieldName == "yaw") {
                token = input.peek()
                yaw = input.nextDouble().toFloat()
                continue
            }
            if (fieldName == "pitch") {
                token = input.peek()
                pitch = input.nextDouble().toFloat()
                continue
            }
            input.skipValue()
        }
        input.endObject()
        val couldNotBeFound: (String) -> String = { name: String ->
            "Property \"$name\" could not be found during deserialization!"
        }
        world ?: throw IllegalStateException(couldNotBeFound("world"))
        x ?: throw IllegalStateException(couldNotBeFound("x"))
        y ?: throw IllegalStateException(couldNotBeFound("y"))
        z ?: throw IllegalStateException(couldNotBeFound("z"))
        yaw ?: throw IllegalStateException(couldNotBeFound("yaw"))
        @Suppress("UNUSED_VALUE")
        pitch ?: throw IllegalStateException(couldNotBeFound("pitch"))
        return Location(Bukkit.getWorld(world), x, y, z, yaw, pitch)
    }
}