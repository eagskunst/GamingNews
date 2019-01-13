package com.eagskunst.emmanuel.gamingnews.fragments.news_list.mvp;

import android.util.Log;

import com.prof.rssparser.Article;
import com.prof.rssparser.OnTaskCompleted;
import com.prof.rssparser.Parser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eagskunst on 09/01/2019
 */
public class NewsListModel implements OnTaskCompleted {

    private NewsListView.OnArticlesLoadedListener listener;

    public void getArticlesFromUrl(String url){
        Parser parser = new Parser();
        parser.onFinish(this);
        parser.execute(url);
    }

    @Override
    public void onTaskCompleted(@NotNull List<Article> arrayList) {
        Log.d("NewsListModel", "onTaskCompleted: parsed!");
        listener.onGetArticlesSucess(arrayList);
    }

    @Override
    public void onError(Exception e) {
        listener.onGetArticlesFail();
        Log.e("NewListModel", "onError: Error parsing"+e.getMessage(), e);
    }

    public void setListener(NewsListView.OnArticlesLoadedListener listener) {
        this.listener = listener;
    }
}
