package me.dkim19375.bedwars.plugin.serializer

import com.google.gson.*
import org.bukkit.Bukkit
import org.bukkit.World
import java.lang.reflect.Type

class WorldSerializer : JsonSerializer<World>,
    JsonDeserializer<World> {
    override fun serialize(
        src: World,
        typeOfSrc: Type,
        context: JsonSerializationContext,
    ): JsonElement {
        val obj = JsonObject()
        obj.addProperty("name", src.name)
        return obj
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext,
    ): World {
        return Bukkit.getWorld(json.asJsonObject.get("name").asString) ?: throw IllegalArgumentException("unknown world")
    }
}