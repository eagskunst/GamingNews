package com.eagskunst.emmanuel.gamingnews.Utility;

import android.content.SharedPreferences;

import com.eagskunst.emmanuel.gamingnews.Models.NewsModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class SharedPreferencesLoader {
    public static void saveList(SharedPreferences.Editor spEditor,List<NewsModel> newsList){
        Gson gson = new Gson();
        String json = gson.toJson(newsList);
        spEditor.putString("SAVED_LIST",json).commit();
    }

    public static List<NewsModel> retrieveList(SharedPreferences sharedPreferences){
        final String listName = sharedPreferences.getString("SAVED_LIST",null);
        if(listName != null){
            Gson gson = new Gson();
            Type type = new TypeToken<List<NewsModel>>(){}.getType();
            List<NewsModel> newsModels = gson.fromJson(listName,type);
            return newsModels;
        }
        else{
            return null;
        }
    }
}
