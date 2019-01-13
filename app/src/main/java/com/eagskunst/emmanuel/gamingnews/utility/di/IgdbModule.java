package com.eagskunst.emmanuel.gamingnews.utility.di;

import com.eagskunst.emmanuel.gamingnews.api.GamesApi;
import com.eagskunst.emmanuel.gamingnews.fragments.releases.ReleasesFragment;
import com.eagskunst.emmanuel.gamingnews.fragments.releases.di.ReleasesScope;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by eagskunst on 11/01/2019
 */
@Module
public class IgdbModule {

    @Provides
    @IgbScope public Retrofit provideRetrofit(){
        return new Retrofit.Builder()
                .baseUrl("https://api-v3.igdb.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Provides
    @IgbScope
    public GamesApi provideApi(Retrofit retrofit){
        return retrofit.create(GamesApi.class);
    }
}
