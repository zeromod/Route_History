package org.zeromod.routehistory.room

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update

@Dao
interface RouteDao{

    @Query("SELECT * FROM route_table")
    fun getAllRoutes():LiveData<List<Route>>

    @Insert
    fun insertRoute(route: Route)

    @Query("SELECT * FROM route_table WHERE id =:id")
    fun getRouteById(id: Int): LiveData<Route>

    @Update
    fun updateRoute(route: Route)
}