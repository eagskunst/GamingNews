package com.example.emmanuel.gamingnews.Utility;

import android.app.Activity;
import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import com.example.emmanuel.gamingnews.Adapter.NewsAdapter;
import com.example.emmanuel.gamingnews.Models.NewsModel;
import com.example.emmanuel.gamingnews.R;
import com.example.emmanuel.gamingnews.views.MainActivity;
import com.prof.rssparser.Article;
import com.prof.rssparser.Parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Handler;


public class ParserMaker{

    private static final String TAG = "ParserMaker";
    private static final int OBJ_NUMBER = 65532;

    private String[] urls;
    private Toast toast;
    private NewsAdapter newsAdapter;
    private boolean running;
    private boolean firstRun;
    private boolean newItems;
    private List<NewsModel> newsList;
    private OnNewsFinishListener onNewsFinishListener;


    public ParserMaker(OnNewsFinishListener onNewsFinishListener,String[] urls, Toast toast, NewsAdapter newsAdapter, List<NewsModel> newsList){
        firstRun = newsList.isEmpty();
        this.urls =  urls;
        this.onNewsFinishListener = onNewsFinishListener;
        this.toast = toast;
        this.newsAdapter = newsAdapter;
        this.newsList = newsList;
    }
    public void create(){
        running = true;
        newItems = false;
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
                if(newsList.isEmpty()){
                    newItems = true;
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
                            newItems = true;
                            newsList.add(new NewsModel(article.getImage() == null ? "":article.getImage(),article.getTitle(),
                                    getDescription(article.getDescription()),article.getLink(),article.getPubDate()));
                        }
                    }
                }
                Log.d(TAG,"Size: "+newsList.size()+" Count: "+newsAdapter.getItemCount());
                if(stopRefresh){
                    if(newItems){
                        newItems = false;
                        firstRun = false;
                        orderListRecentFirst();
                    }
                    newsAdapter.notifyDataSetChanged();
                    newsAdapter.getNewsListCopy().addAll(newsList);
                    refreshingStatus();
                }
            }

            @Override
            public void onError() {
                toast.show();
                if(stopRefresh) {
                    firstRun = !newsList.isEmpty();
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
        onNewsFinishListener.onNewsFinish();
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isRunning() {
        return running;
    }

    public interface OnNewsFinishListener{
        void onNewsFinish();
    }

}
