package com.eagskunst.emmanuel.gamingnews.models

class Categories(var language: String, var all_urls: Array<String>) {
    var ps4_urls: Array<String>? = null
    var xboxO_urls: Array<String>? = null
    var switch_urls: Array<String>? = null
    var pc_urls: Array<String>? = null

    override fun toString(): String {
        return "Categories{" +
                "language='" + language + '\'' +
                ", all_urls=" + all_urls.contentToString() +
                '}'
    }
}
