package com.eagskunst.emmanuel.gamingnews.Utility;

import android.content.SharedPreferences;
import android.content.res.Resources;

import com.eagskunst.emmanuel.gamingnews.Models.NewsModel;
import com.eagskunst.emmanuel.gamingnews.R;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
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

    public static void saveTopics(SharedPreferences.Editor spEditor, List<String> topics){
        Gson gson = new Gson();
        String toJson = gson.toJson(topics);
        spEditor.putString("TOPIC_LIST",toJson).apply();
    }

    public static List<String> retrieveTopics(SharedPreferences sharedPreferences){
        final String listName = sharedPreferences.getString("TOPIC_LIST",null);
        if(listName!= null){
            Gson gson = new Gson();
            Type type = new TypeToken<List<String>>(){}.getType();
            List<String> topics = gson.fromJson(listName,type);
            return topics;
        }
        else{
            return null;
        }
    }

    public static void saveFirebaseToken(SharedPreferences.Editor spEditor, String token){
        spEditor.putString("USER_TOKEN",token).apply();
    }

    public static String getFirebaseToken(SharedPreferences sharedPreferences){
        return sharedPreferences.getString("USER_TOKEN","no_play_services");
    }

}
