package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.ArticleEntity
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.ReleaseEntity

@Database(
    entities = [ArticleEntity::class, ReleaseEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
    abstract fun releaseDao(): ReleaseDao
}
