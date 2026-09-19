package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE articles ADD COLUMN author TEXT")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `mute_rules` (
                `id` TEXT NOT NULL,
                `text` TEXT NOT NULL,
                `matchMode` TEXT NOT NULL,
                `caseSensitive` INTEGER NOT NULL,
                `appliesEverywhere` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `mute_rule_tabs` (
                `ruleId` TEXT NOT NULL,
                `category` TEXT NOT NULL,
                PRIMARY KEY(`ruleId`, `category`),
                FOREIGN KEY(`ruleId`) REFERENCES `mute_rules`(`id`)
                    ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_mute_rule_tabs_ruleId` ON `mute_rule_tabs` (`ruleId`)"
        )
    }
}
