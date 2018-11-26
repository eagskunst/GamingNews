package com.eagskunst.emmanuel.gamingnews.utility;

import android.content.SharedPreferences;

import com.eagskunst.emmanuel.gamingnews.models.NewsModel;
import com.eagskunst.emmanuel.gamingnews.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
            List<NewsModel> newsModels = new ArrayList<>();
            return newsModels;
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

    public static void saveCurrentTime(SharedPreferences.Editor spEditor){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        String session = formatter.format(date);
        spEditor.putString("LAST_SESSION",session);
    }

    public static String getLastSession(SharedPreferences sharedPreferences){
        return sharedPreferences.getString("LAST_SESSION",null);
    }

}
