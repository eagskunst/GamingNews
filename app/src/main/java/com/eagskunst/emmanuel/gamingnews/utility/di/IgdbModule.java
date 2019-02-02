package com.eagskunst.emmanuel.gamingnews.utility.di;

import com.eagskunst.emmanuel.gamingnews.api.GamesApi;
import com.eagskunst.emmanuel.gamingnews.models.Cover;
import com.eagskunst.emmanuel.gamingnews.utility.CoverConverter;
import com.google.gson.Gson;

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
    @IgbScope
    public Retrofit provideRetrofit(){
        Gson gson = new Gson().newBuilder()
                .registerTypeAdapter(Cover.class, new CoverConverter())
                .create();
        GsonConverterFactory gsonConverterFactory = GsonConverterFactory.create(gson);
        return new Retrofit.Builder()
                .baseUrl("https://api-v3.igdb.com/")
                .addConverterFactory(gsonConverterFactory)
                .build();
    }

    @Provides
    @IgbScope
    public GamesApi provideApi(Retrofit retrofit){
        return retrofit.create(GamesApi.class);
    }
}
