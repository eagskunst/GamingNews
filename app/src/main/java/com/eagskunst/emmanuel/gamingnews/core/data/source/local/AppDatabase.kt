package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.ArticleEntity
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.MuteRuleEntity
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.MuteRuleTabEntity
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.ReleaseCoverageEntity
import com.eagskunst.emmanuel.gamingnews.core.data.source.local.entity.ReleaseEntity

@Database(
    entities = [
        ArticleEntity::class,
        ReleaseEntity::class,
        ReleaseCoverageEntity::class,
        MuteRuleEntity::class,
        MuteRuleTabEntity::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
    abstract fun releaseDao(): ReleaseDao
    abstract fun muteRuleDao(): MuteRuleDao
}
