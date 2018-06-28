package com.example.emmanuel.gamingnews.Utility;

import android.app.Activity;
import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.emmanuel.gamingnews.Adapter.NewsAdapter;
import com.example.emmanuel.gamingnews.Models.NewsModel;
import com.example.emmanuel.gamingnews.R;
import com.prof.rssparser.Article;
import com.prof.rssparser.Parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ParserMaker{

    private static final String TAG = "ParserMaker";
    private static final int OBJ_NUMBER = 65532;

    private Activity activity;
    private String[] urls;
    private Toast toast;
    private NewsAdapter newsAdapter;
    private boolean running;
    private boolean firstRun;
    private boolean error;
    private List<NewsModel> newsList;

    public ParserMaker(Activity activity,String[] urls, Toast toast, NewsAdapter newsAdapter, List<NewsModel> newsList){
        firstRun = true;
        this.urls =  urls;
        this.activity = activity;
        this.toast = toast;
        this.newsAdapter = newsAdapter;
        this.newsList = newsList;
    }
    public void create(){
        running = true;
        error = false;
        Parser[] parsers = new Parser[urls.length];
        for (int i = 0;i<parsers.length;i++){
            parsers[i] = new Parser();
            parsers[i].onFinish(taskCompleted((i + 1) == parsers.length));
            parsers[i].execute(urls[i]);
        }
    }

    private Parser.OnTaskCompleted taskCompleted(final boolean stopRefresh){
        return new Parser.OnTaskCompleted() {
            @Override
            public void onTaskCompleted(ArrayList<Article> list) {
                if(firstRun){
                    for(com.prof.rssparser.Article article:list){
                        newsList.add(new NewsModel(article.getImage() == null ? "":article.getImage(),article.getTitle(),
                                getDescription(article.getDescription()),article.getLink(),article.getPubDate()));
                    }
                }
                else {
                    for(com.prof.rssparser.Article article:list){
                        int aux = 0;

                        for (NewsModel news:newsList) {
                            if(news.getTitle().equals(article.getTitle())){
                                aux = 1;
                                break;
                            }
                        }
                        if(aux == 0){
                            newsList.add(new NewsModel(article.getImage() == null ? "":article.getImage(),article.getTitle(),
                                    getDescription(article.getDescription()),article.getLink(),article.getPubDate()));
                        }
                    }
                }
                Log.d(TAG,"Size: "+newsList.size()+" Count: "+newsAdapter.getItemCount());
                if(stopRefresh){
                    firstRun = false;
                    if(error){
                        toast.show();
                    }
                    if(firstRun){
                        orderListRecentFirst();
                    }
                    newsAdapter.notifyDataSetChanged();
                    refreshingStatus();
                    running = false;
                }
            }

            @Override
            public void onError() {
                error = true;
                if(stopRefresh){
                    if(newsList.size() != 0){
                        firstRun = false;
                    }
                    toast.show();
                    running = false;
                    refreshingStatus();
                }
            }
        };
    }


    private String getDescription(String content){
        String mString="";
        if(!content.isEmpty()){
            content = Html.fromHtml(content).toString().replace((char) OBJ_NUMBER,' ').trim();
            if(content.indexOf('.') != -1){
                mString = content.substring(0,content.indexOf('.'))+".";
            }
            else{
                mString = content+"...";
            }
            if(mString.length()>180){
                mString = (mString.substring(0,175))+"...";
            }
        }

        return mString;
    }

    private void orderListRecentFirst() {
        Collections.sort(newsList, new Comparator<NewsModel>() {
            @Override
            public int compare(NewsModel newsModel, NewsModel t1) {
                if(newsModel.getPubDate() != null && t1.getPubDate() != null){
                    return t1.getPubDate().compareTo(newsModel.getPubDate());
                }
                else{
                    return 1;
                }
            }
        });
    }

    private void refreshingStatus(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((SwipeRefreshLayout)activity.findViewById(R.id.refreshlayout)).setRefreshing(false);
            }
        });

    }
    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity){
        this.activity = activity;
    }

    public boolean isRunning() {
        return running;
    }
}
