package com.eagskunst.emmanuel.gamingnews.fragments.news_list.di;

import com.eagskunst.emmanuel.gamingnews.fragments.news_list.NewsListFragment;
import com.eagskunst.emmanuel.gamingnews.fragments.news_list.mvp.NewsListModel;
import com.eagskunst.emmanuel.gamingnews.fragments.news_list.mvp.NewsListPresenter;
import com.eagskunst.emmanuel.gamingnews.models.NewsModel;

import dagger.Module;
import dagger.Provides;

/**
 * Created by eagskunst on 09/01/2019
 */

@Module
public class NewsModule {
    private NewsListFragment newsListFragment;

    public NewsModule(NewsListFragment newsListFragment){
        this.newsListFragment = newsListFragment;
    }

    @Provides
    @NewsScope
    public NewsListModel provideModel(){
        return new NewsListModel();
    }

    @Provides
    @NewsScope
    public NewsListPresenter providePresenter(NewsListModel newsListModel){
        return new NewsListPresenter(newsListModel);
    }

}
