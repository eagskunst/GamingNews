package com.eagskunst.emmanuel.gamingnews.fragments.news_list.di;

import com.eagskunst.emmanuel.gamingnews.fragments.news_list.NewsListFragment;

import dagger.Component;

/**
 * Created by eagskunst on 09/01/2019
 */
@NewsScope
@Component (modules = {NewsModule.class})

public interface NewsComponent {
    void inject(NewsListFragment newsListFragment);
}
