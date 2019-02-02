package com.eagskunst.emmanuel.gamingnews.utility;

import com.eagskunst.emmanuel.gamingnews.models.Cover;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

import java.lang.reflect.Type;

/**
 * Created by eagskunst on 18/01/2019
 * Igdb API sending an Integer instead of an empty/null object make my do this...
 * https://stackoverflow.com/a/29898073/10084458
 */

public class CoverConverter implements JsonDeserializer<Cover> {

    public Cover deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx){
        Cover cover = null;
        if(json.isJsonPrimitive()){
            cover = new Cover(json.getAsInt());
        }
        else if(json.isJsonObject()){
            cover = new Cover();
            cover.setId(json.getAsJsonObject().get("id").getAsInt());
            cover.setUrl(json.getAsJsonObject().get("url").getAsString());
        }

        return cover;
    }
}
