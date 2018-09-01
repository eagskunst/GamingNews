package com.eagskunst.emmanuel.gamingnews.Utility;

import android.content.SharedPreferences;
import android.content.res.Resources;

import com.eagskunst.emmanuel.gamingnews.Models.NewsModel;
import com.eagskunst.emmanuel.gamingnews.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class SharedPreferencesLoader {
    public static boolean canLoadImages = true;
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

    public static int currentTheme(SharedPreferences sharedPreferences){
        boolean isDark = sharedPreferences.getBoolean("night_mode",false);
        if(!isDark){
            return R.style.AppTheme;
        }
        else
            return R.style.AppThemeDark;
    }

    public static void setCanLoadImages(SharedPreferences sharedPreferences){
        boolean loadImages = sharedPreferences.getBoolean("load_images",true);
        SharedPreferencesLoader.canLoadImages = loadImages;
    }
}
