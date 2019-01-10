package com.eagskunst.emmanuel.gamingnews.fragments.news_list.mvp;

import com.prof.rssparser.Article;
import com.prof.rssparser.Parser;

import java.util.ArrayList;

/**
 * Created by eagskunst on 09/01/2019
 */
public class NewsListModel implements Parser.OnTaskCompleted{

    private NewsListView.OnArticlesLoadedListener listener;

    public void getArticlesFromUrl(String url){
        Parser parser = new Parser();
        parser.onFinish(this);
        parser.execute(url);
    }

    @Override
    public void onTaskCompleted(ArrayList<Article> arrayList) {
        listener.onGetArticlesSucess(arrayList);
    }

    @Override
    public void onError() {
        listener.onGetArticlesFail();
    }

    public void setListener(NewsListView.OnArticlesLoadedListener listener) {
        this.listener = listener;
    }
}
