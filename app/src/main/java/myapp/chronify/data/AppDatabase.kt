/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package myapp.chronify.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteTable
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import myapp.chronify.data.nife.Nife
import myapp.chronify.data.nife.NifeConverters
import myapp.chronify.data.nife.NifeDao
import myapp.chronify.data.schedule.ScheduleDao
import myapp.chronify.data.schedule.ScheduleEntity

@Database(
    entities = [Nife::class],
    version = 4,
    exportSchema = true,
)
@TypeConverters(NifeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    // get DAOs
    abstract fun nifeDao(): NifeDao
    // abstract fun scheduleDao(): ScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // 定义迁移对象
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 删除旧表（如果需要）
                db.execSQL("DROP TABLE IF EXISTS Schedule")

                // 创建新表（Room会自动生成Nife表的SQL）
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `Nife` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `isFinished` INTEGER NOT NULL,
                        `createdDT` INTEGER NOT NULL,
                        `beginDT` INTEGER,
                        `endDT` INTEGER,
                        `period` TEXT,
                        `periodMultiple` INTEGER NOT NULL,
                        `triggerTimes` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `location` TEXT NOT NULL
                    )
                    """
                )
            }
        }
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 删除旧表（如果需要）
                db.execSQL("DROP TABLE IF EXISTS ScheduleEntity")
            }
        }
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 使用 "销毁与重建策略"
                db.execSQL(
                    """
                    CREATE TABLE tmp_YourEntity (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `isFinished` INTEGER NOT NULL,
                        `createdDT` INTEGER NOT NULL,
                        `beginDT` INTEGER,
                        `endDT` INTEGER,
                        `period` TEXT,
                        `periodMultiple` INTEGER NOT NULL,
                        `triggerTimes` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `location` TEXT NOT NULL
                    )
                    """
                )
                // 复制旧数据到临时表
                db.execSQL("INSERT INTO tmp_YourEntity SELECT * FROM Nife")
                // 删除旧表
                db.execSQL("DROP TABLE Nife")
                // 重命名临时表
                db.execSQL("ALTER TABLE tmp_YourEntity RENAME TO Nife")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "Chronify_database"
                )
                    // .createFromAsset("database/bus_schedule.db")
                    // Wipes and rebuilds instead of migrating if no Migration object.
                    // .fallbackToDestructiveMigration()
                    .addMigrations(MIGRATION_2_3)
                    .addMigrations(MIGRATION_3_4)
                    .build()
                    .also {
                        INSTANCE = it
                    }
            }
        }
    }
}
