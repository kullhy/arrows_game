package com.batodev.arrows.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS game_boards (
                boardId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                stateType TEXT NOT NULL,
                width INTEGER NOT NULL,
                height INTEGER NOT NULL
            )"""
        )
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS snakes (
                snakeRowId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                boardId INTEGER NOT NULL,
                snakeId INTEGER NOT NULL,
                headDirection TEXT NOT NULL,
                FOREIGN KEY (boardId) REFERENCES game_boards(boardId) ON DELETE CASCADE
            )"""
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_snakes_boardId ON snakes(boardId)")
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS snake_body_points (
                pointId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                snakeRowId INTEGER NOT NULL,
                orderIndex INTEGER NOT NULL,
                x INTEGER NOT NULL,
                y INTEGER NOT NULL,
                FOREIGN KEY (snakeRowId) REFERENCES snakes(snakeRowId) ON DELETE CASCADE
            )"""
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_snake_body_points_snakeRowId ON snake_body_points(snakeRowId)")

        db.execSQL(
            """CREATE TABLE user_preferences_new (
                id INTEGER PRIMARY KEY NOT NULL,
                theme TEXT NOT NULL DEFAULT 'Green',
                animationSpeed TEXT NOT NULL DEFAULT 'Medium',
                isVibrationEnabled INTEGER NOT NULL DEFAULT 1,
                isSoundsEnabled INTEGER NOT NULL DEFAULT 1,
                isFillBoardEnabled INTEGER NOT NULL DEFAULT 0,
                levelNumber INTEGER NOT NULL DEFAULT 1,
                currentLives INTEGER NOT NULL DEFAULT 5,
                debugForcedWidth INTEGER,
                debugForcedHeight INTEGER,
                debugForcedLives INTEGER,
                debugForcedShape TEXT,
                isAdFree INTEGER NOT NULL DEFAULT 0,
                rewardAdCount INTEGER NOT NULL DEFAULT 0,
                gamesCompleted INTEGER NOT NULL DEFAULT 0,
                introCompleted INTEGER NOT NULL DEFAULT 0,
                isWinVideosEnabled INTEGER NOT NULL DEFAULT 0
            )"""
        )
        db.execSQL(
            """INSERT INTO user_preferences_new (
                id, theme, animationSpeed, isVibrationEnabled, isSoundsEnabled,
                isFillBoardEnabled, levelNumber, currentLives,
                debugForcedWidth, debugForcedHeight, debugForcedLives, debugForcedShape,
                isAdFree, rewardAdCount, gamesCompleted, introCompleted, isWinVideosEnabled
            ) SELECT
                id, theme, animationSpeed, isVibrationEnabled, isSoundsEnabled,
                isFillBoardEnabled, levelNumber, currentLives,
                debugForcedWidth, debugForcedHeight, debugForcedLives, debugForcedShape,
                isAdFree, rewardAdCount, gamesCompleted, introCompleted, isWinVideosEnabled
            FROM user_preferences"""
        )
        db.execSQL("DROP TABLE user_preferences")
        db.execSQL("ALTER TABLE user_preferences_new RENAME TO user_preferences")
    }
}

@Database(
    entities = [
        UserPreferencesEntity::class,
        GameBoardEntity::class,
        SnakeEntity::class,
        SnakeBodyPointEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userPreferencesDao(): UserPreferencesDao
    abstract fun gameStateDao(): GameStateDao
}
