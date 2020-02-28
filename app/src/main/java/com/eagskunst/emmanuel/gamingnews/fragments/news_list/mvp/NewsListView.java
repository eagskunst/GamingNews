package com.eagskunst.emmanuel.gamingnews.fragments.news_list.mvp;

import com.eagskunst.emmanuel.gamingnews.models.NewsModel;
import com.prof.rssparser.Article;
import com.prof.rssparser.Channel;

import java.util.List;

/**
 * Created by eagskunst on 09/01/2019
 */
public interface NewsListView {

    interface Presenter{
        void onCreateView(NewsListView.View view);
        void onDestroyView();
        void getArticles(String urls[]);
        String formatDescription(String content);
        void sortListByDate();
    }

    interface OnArticlesLoadedListener {
        void onGetArticlesSuccess(List<Article> articles, Channel channel);
        void onGetArticlesFail();
    }

    interface View {
        void updateList(List<NewsModel> newsList);
        void getArticleList();
        boolean checkInternetConnection();
        void createAlertDialog(int message);
        void showToastError(String message);
        void showToastError(int message);
        void loadListFromSharedPreferences();
    }
}
