package me.dkim19375.bedwars.plugin.serializer

import com.google.gson.*
import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.data.MainShopConfigItem
import java.lang.reflect.Type

class ShopConfigItemSerializer(private val plugin: BedwarsPlugin) : JsonSerializer<MainShopConfigItem>,
    JsonDeserializer<MainShopConfigItem> {
    override fun serialize(
        src: MainShopConfigItem,
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
    ): MainShopConfigItem {
        return plugin.configManager.getItemFromName(json.asJsonObject.get("name").asString)
            ?: throw IllegalStateException("Property \"name\" could not be found during deserialization!")
    }
}