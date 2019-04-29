package org.zeromod.routehistory.room

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "route_table")
data class Route(
    val location: String,
    val distance:Float,
    @ColumnInfo(name = "start_time")val startTime: String,
    @ColumnInfo(name = "end_time")val endTime: String,
    val duration: Long,
    var notes: String,
    @PrimaryKey(autoGenerate = true) val id: Int = 0)
