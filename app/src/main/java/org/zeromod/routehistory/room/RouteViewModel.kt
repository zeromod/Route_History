package org.zeromod.routehistory.room

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class RouteViewModel(application: Application) : AndroidViewModel(application){

    private var parentJob = Job()

    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)
    private val repository: RouteRepository


    val allRoutes : LiveData<List<Route>>

    init {
        val routeDao = RouteRoomDatabase.getDatabase(application).routeDao()
        repository = RouteRepository(routeDao)
        allRoutes = repository.allRotutes
    }

    fun insert(route: Route) = scope.launch(Dispatchers.IO) {
        repository.addRoute(route)
    }

    fun update(route: Route) = scope.launch (Dispatchers.IO) {
        repository.updateRoute(route)
    }

    fun getRouteById(id: Int): LiveData<Route>{
        return repository.getRouteById(id)
    }

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }
}