package com.eagskunst.emmanuel.gamingnews.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.eagskunst.emmanuel.gamingnews.Models.NewsModel;
import com.eagskunst.emmanuel.gamingnews.R;
import com.eagskunst.emmanuel.gamingnews.Utility.SharedPreferencesLoader;

import java.util.List;

/**
 * Created by eagskunst on 09/11/2018
 */
public class SaveArticleReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPreferences",Context.MODE_PRIVATE);
        List<NewsModel> savedList = SharedPreferencesLoader.retrieveList(sharedPreferences);
        Bundle b = intent.getBundleExtra("Bundle");
        NewsModel article = b.getParcelable("Article");
        Log.d(SaveArticleReceiver.class.getSimpleName(), "onReceive: entered");
        boolean isSaved = false;
        for(NewsModel n:savedList){
            n.getLink();
            article.getLink();
            if(n.getLink().equals(article.getLink())){
                article = n;
                isSaved = true;
            }
        }
        if(!isSaved){
            savedList.add(0,article);
            Toast.makeText(context, R.string.article_saved, Toast.LENGTH_SHORT).show();
        }
        else{
            savedList.remove(article);
            Toast.makeText(context, R.string.article_removed, Toast.LENGTH_SHORT).show();
        }
        SharedPreferencesLoader.saveList(sharedPreferences.edit(),savedList);
    }
}
