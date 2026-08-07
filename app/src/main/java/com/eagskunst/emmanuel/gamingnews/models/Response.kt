package com.eagskunst.emmanuel.gamingnews.models

import com.google.gson.annotations.SerializedName

class Response {

    @SerializedName("date")
    var date: Int = 0

    @SerializedName("game")
    var game: Game? = null

    @SerializedName("updated_at")
    var updatedAt: Int = 0

    @SerializedName("created_at")
    var createdAt: Int = 0

    @SerializedName("y")
    var y: Int = 0

    @SerializedName("id")
    var id: Int = 0

    @SerializedName("category")
    var category: Int = 0

    @SerializedName("human")
    var human: String? = null

    @SerializedName("m")
    var m: Int = 0

    @SerializedName("platform")
    var platform: Int = 0

    override fun toString(): String {
        return "Response{" +
                "date = '" + date + '\'' +
                ",game = '" + game + '\'' +
                ",updated_at = '" + updatedAt + '\'' +
                ",created_at = '" + createdAt + '\'' +
                ",y = '" + y + '\'' +
                ",id = '" + id + '\'' +
                ",category = '" + category + '\'' +
                ",human = '" + human + '\'' +
                ",m = '" + m + '\'' +
                ",platform = '" + platform + '\'' +
                "}"
    }
}
