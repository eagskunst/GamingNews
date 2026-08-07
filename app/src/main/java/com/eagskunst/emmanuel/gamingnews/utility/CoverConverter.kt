package com.eagskunst.emmanuel.gamingnews.utility

import com.eagskunst.emmanuel.gamingnews.models.Cover
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * Created by eagskunst on 18/01/2019
 * Igdb API sending an Integer instead of an empty/null object make my do this...
 * https://stackoverflow.com/a/29898073/10084458
 */
class CoverConverter : JsonDeserializer<Cover> {

    override fun deserialize(json: JsonElement, typeOfT: Type, ctx: JsonDeserializationContext): Cover? {
        var cover: Cover? = null
        if (json.isJsonPrimitive) {
            cover = Cover(json.asInt)
        } else if (json.isJsonObject) {
            cover = Cover()
            cover.id = json.asJsonObject.get("id").asInt
            cover.url = json.asJsonObject.get("url").asString
        }
        return cover
    }
}
