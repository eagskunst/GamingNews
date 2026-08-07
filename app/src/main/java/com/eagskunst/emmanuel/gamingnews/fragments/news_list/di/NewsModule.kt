package com.eagskunst.emmanuel.gamingnews.fragments.news_list.di

import com.eagskunst.emmanuel.gamingnews.fragments.news_list.NewsListFragment
import com.eagskunst.emmanuel.gamingnews.fragments.news_list.mvp.NewsListModel
import com.eagskunst.emmanuel.gamingnews.fragments.news_list.mvp.NewsListPresenter
import dagger.Module
import dagger.Provides

/**
 * Created by eagskunst on 09/01/2019
 */
@Module
class NewsModule(private val newsListFragment: NewsListFragment) {

    @Provides
    @NewsScope
    fun provideModel(): NewsListModel {
        return NewsListModel()
    }

    @Provides
    @NewsScope
    fun providePresenter(newsListModel: NewsListModel): NewsListPresenter {
        return NewsListPresenter(newsListModel)
    }
}
