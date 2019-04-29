package org.zeromod.routehistory.room

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

@Database(entities = [Route::class], version = 1)
abstract class RouteRoomDatabase : RoomDatabase() {
    abstract fun routeDao() : RouteDao

    companion object {
        @Volatile
        private var INSTANCE: RouteRoomDatabase? = null

        fun getDatabase(context: Context): RouteRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RouteRoomDatabase::class.java,
                    "Route_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}