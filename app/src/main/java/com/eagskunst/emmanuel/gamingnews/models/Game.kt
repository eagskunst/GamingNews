package com.eagskunst.emmanuel.gamingnews.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Game {

    @SerializedName("cover")
    @Expose
    var cover: Cover? = null

    @SerializedName("name")
    var name: String? = null

    @SerializedName("id")
    var id: Int = 0

    @SerializedName("url")
    var gameUrl: String? = null

    override fun toString(): String {
        return "Game{" +
                "cover=" + cover +
                ", name='" + name + '\'' +
                ", id=" + id +
                ", gameUrl='" + gameUrl + '\'' +
                '}'
    }
}
