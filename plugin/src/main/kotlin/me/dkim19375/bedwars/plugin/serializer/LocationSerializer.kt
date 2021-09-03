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