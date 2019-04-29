package org.zeromod.routehistory.room

import android.arch.lifecycle.LiveData
import android.support.annotation.WorkerThread

class RouteRepository(private  val routeDao: RouteDao){

    val allRotutes : LiveData<List<Route>> = routeDao.getAllRoutes()

    @WorkerThread
    fun addRoute(route: Route){
        routeDao.insertRoute(route)
    }

    @WorkerThread
    fun updateRoute(route: Route){
        routeDao.updateRoute(route)
    }

    fun getRouteById(id: Int): LiveData<Route>{
       return routeDao.getRouteById(id)
    }
}