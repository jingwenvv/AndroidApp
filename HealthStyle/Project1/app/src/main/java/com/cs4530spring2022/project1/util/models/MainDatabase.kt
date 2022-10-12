package com.cs4530spring2022.project1.util.models

import android.content.Context
import android.os.Looper
import androidx.core.os.HandlerCompat
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.Executors


@Database(entities = [UserProfile::class, WeatherTable::class], version = 1, exportSchema = false)
abstract class MainDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun weatherDao(): WeatherDao

    companion object {
        @Volatile
        private var mInstance: MainDatabase? = null
        @Synchronized
        fun getDatabase(context: Context): MainDatabase? {
            if (mInstance == null) {
                mInstance = Room.databaseBuilder(
                    context.applicationContext,
                    MainDatabase::class.java, "user.db"
                ).fallbackToDestructiveMigration().build()
            }
            return mInstance
        }
    }
}
