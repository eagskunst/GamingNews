package com.eagskunst.emmanuel.gamingnews.fragments.news_list.mvp;

import android.util.Log;

import com.prof.rssparser.Channel;
import com.prof.rssparser.OnTaskCompleted;
import com.prof.rssparser.Parser;

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
    public void onTaskCompleted(Channel channel) {
        Log.d("NewsListModel", "onTaskCompleted: parsed!");
        listener.onGetArticlesSuccess(channel.getArticles(), channel);
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
