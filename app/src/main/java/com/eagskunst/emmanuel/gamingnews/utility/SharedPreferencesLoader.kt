package com.eagskunst.emmanuel.gamingnews.utility

import android.content.SharedPreferences
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.models.NewsModel
import com.eagskunst.emmanuel.gamingnews.models.ReleasesModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date

object SharedPreferencesLoader {
    @JvmField
    var canLoadImages = true

    @JvmStatic
    fun saveList(spEditor: SharedPreferences.Editor, newsList: List<NewsModel>) {
        val gson = Gson()
        val json = gson.toJson(newsList)
        spEditor.putString("SAVED_LIST", json).commit()
    }

    @JvmStatic
    fun retrieveList(sharedPreferences: SharedPreferences): List<NewsModel> {
        val listName = sharedPreferences.getString("SAVED_LIST", null)
        return if (listName != null) {
            val gson = Gson()
            val type = object : TypeToken<List<NewsModel>>() {}.type
            gson.fromJson(listName, type)
        } else {
            ArrayList()
        }
    }

    @JvmStatic
    fun currentTheme(sharedPreferences: SharedPreferences): Int {
        val isDark = sharedPreferences.getBoolean("night_mode", false)
        return if (!isDark) R.style.AppTheme else R.style.AppThemeDark
    }

    @JvmStatic
    fun setCanLoadImages(sharedPreferences: SharedPreferences) {
        val loadImages = sharedPreferences.getBoolean("load_images", true)
        canLoadImages = loadImages
    }

    @JvmStatic
    fun saveTopics(spEditor: SharedPreferences.Editor, topics: List<String>) {
        val gson = Gson()
        val toJson = gson.toJson(topics)
        spEditor.putString("TOPIC_LIST", toJson).apply()
    }

    @JvmStatic
    fun retrieveTopics(sharedPreferences: SharedPreferences): List<String>? {
        val listName = sharedPreferences.getString("TOPIC_LIST", null)
        return if (listName != null) {
            val gson = Gson()
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(listName, type)
        } else {
            null
        }
    }

    @JvmStatic
    fun saveFirebaseToken(spEditor: SharedPreferences.Editor, token: String) {
        spEditor.putString("USER_TOKEN", token).apply()
    }

    @JvmStatic
    fun getFirebaseToken(sharedPreferences: SharedPreferences): String {
        return sharedPreferences.getString("USER_TOKEN", "no_token")!!
    }

    @JvmStatic
    fun saveCurrentTime(spEditor: SharedPreferences.Editor) {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        val date = Date()
        val session = formatter.format(date)
        spEditor.putString("LAST_SESSION", session)
    }

    @JvmStatic
    fun getLastSession(sharedPreferences: SharedPreferences): String? {
        return sharedPreferences.getString("LAST_SESSION", null)
    }

    @JvmStatic
    fun saveReleasesList(spEditor: SharedPreferences.Editor, list: List<ReleasesModel>) {
        val gson = Gson()
        val toJson = gson.toJson(list)
        spEditor.putString("RELEASES_LIST", toJson).apply()
    }

    @JvmStatic
    fun retrieveReleasesList(sharedPreferences: SharedPreferences): List<ReleasesModel>? {
        val listName = sharedPreferences.getString("RELEASES_LIST", null)
        return if (listName != null) {
            val gson = Gson()
            val type = object : TypeToken<List<ReleasesModel>>() {}.type
            gson.fromJson(listName, type)
        } else {
            null
        }
    }

    @JvmStatic
    fun saveCurrentMonth(editor: SharedPreferences.Editor, month: Int) {
        editor.putInt("saved_month", month)
        editor.apply()
    }

    @JvmStatic
    fun getSavedMonth(sharedPreferences: SharedPreferences): Int {
        return sharedPreferences.getInt("saved_month", -1)
    }
}
