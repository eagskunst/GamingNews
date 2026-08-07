package com.eagskunst.emmanuel.gamingnews.objects

import com.eagskunst.emmanuel.gamingnews.models.Categories
import com.google.gson.Gson
import java.io.IOException
import java.io.InputStream

class LoadUrls(private val language: String, private val jsonFile: InputStream) {
    var allUrls: Array<String>? = null
    var ps4Urls: Array<String>? = null
    var xboxOUrls: Array<String>? = null
    var switchUrls: Array<String>? = null
    var pcUrls: Array<String>? = null

    private fun loadCategories(): Array<Categories> {
        val gson = Gson()
        return gson.fromJson(loadJSONFromAsset(), Array<Categories>::class.java)
    }

    private fun getLocalCategory(): Categories? {
        val categories = loadCategories()
        var category: Categories? = null
        for (cat in categories) {
            if (cat.language == language) {
                category = cat
                break
            } else if (cat.language == "en" && category == null) {
                category = cat
            }
        }
        return category
    }

    fun setUrls() {
        val category = getLocalCategory()
        allUrls = category?.all_urls
        ps4Urls = category?.ps4_urls
        xboxOUrls = category?.xboxO_urls
        switchUrls = category?.switch_urls
        pcUrls = category?.pc_urls
    }

    private fun loadJSONFromAsset(): String? {
        var json: String? = null
        try {
            val size = jsonFile.available()
            val buffer = ByteArray(size)
            jsonFile.read(buffer)
            jsonFile.close()
            json = String(buffer, Charsets.UTF_8)
            return json
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return json
    }
}
