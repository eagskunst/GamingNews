package com.eagskunst.emmanuel.gamingnews.models

import com.google.gson.annotations.SerializedName

class Cover() {

    @SerializedName("id")
    var id: Int = 0

    @SerializedName("url")
    var url: String? = null

    constructor(id: Int, url: String?) : this() {
        this.id = id
        this.url = url
    }

    constructor(id: Int) : this() {
        this.id = id
        this.url = null
    }

    override fun toString(): String {
        return "Cover{" +
                "id = '" + id + '\'' +
                ",url = '" + url + '\'' +
                "}"
    }
}
