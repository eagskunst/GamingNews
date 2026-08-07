package com.eagskunst.emmanuel.gamingnews.di.module

import com.eagskunst.emmanuel.gamingnews.BuildConfig
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.IgdbApi
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.api.TwitchAuthApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private val json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    @Named("igdb")
    fun provideIgdbRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.igdb.com/v4/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    @Named("twitch")
    fun provideTwitchRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://id.twitch.tv/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideIgdbApi(@Named("igdb") retrofit: Retrofit): IgdbApi = retrofit.create(IgdbApi::class.java)

    @Provides
    @Singleton
    fun provideTwitchAuthApi(@Named("twitch") retrofit: Retrofit): TwitchAuthApi =
        retrofit.create(TwitchAuthApi::class.java)

    @Provides
    @Named("igdbClientId")
    fun provideIgdbClientId(): String = BuildConfig.TWITCH_CLIENT_ID

    @Provides
    @Named("twitchClientId")
    fun provideTwitchClientId(): String = BuildConfig.TWITCH_CLIENT_ID

    @Provides
    @Named("twitchClientSecret")
    fun provideTwitchClientSecret(): String = BuildConfig.TWITCH_CLIENT_SECRET
}
