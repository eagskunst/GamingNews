package com.eagskunst.emmanuel.gamingnews.utility.di

import com.eagskunst.emmanuel.gamingnews.api.GamesApi
import com.eagskunst.emmanuel.gamingnews.models.Cover
import com.eagskunst.emmanuel.gamingnews.utility.CoverConverter
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by eagskunst on 11/01/2019
 */
@Module
class IgdbModule {

    @Provides
    @IgbScope
    fun provideRetrofit(): Retrofit {
        val gson = Gson().newBuilder()
            .registerTypeAdapter(Cover::class.java, CoverConverter())
            .create()
        val gsonConverterFactory = GsonConverterFactory.create(gson)
        return Retrofit.Builder()
            .baseUrl("https://api-v3.igdb.com/")
            .addConverterFactory(gsonConverterFactory)
            .build()
    }

    @Provides
    @IgbScope
    fun provideApi(retrofit: Retrofit): GamesApi {
        return retrofit.create(GamesApi::class.java)
    }
}
