package com.eagskunst.emmanuel.gamingnews.di.module

import android.content.Context
import androidx.room.Room
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.AppDatabase
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.ArticleDao
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.ReleaseDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "gamingnews.db"
        ).build()
    }

    @Provides
    fun provideArticleDao(database: AppDatabase): ArticleDao = database.articleDao()

    @Provides
    fun provideReleaseDao(database: AppDatabase): ReleaseDao = database.releaseDao()
}
