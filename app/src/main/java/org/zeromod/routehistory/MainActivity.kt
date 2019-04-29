package org.zeromod.routehistory

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.text.SimpleDateFormat
import java.util.*
import org.zeromod.routehistory.room.Route
import org.zeromod.routehistory.room.RouteViewModel


class MainActivity : AppCompatActivity() {

    private lateinit var routeViewModel: RouteViewModel
    var locationManager: LocationManager? = null
    val routeList: MutableList<String> = mutableListOf()
    var isStarted = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        checkPermission()


        val adapter = RouteListAdapter(this)
        routeViewModel = ViewModelProviders.of(this).get(RouteViewModel::class.java)
        routeViewModel.allRoutes.observe(this, Observer { routes ->
            routes?.let {
                //Show empty data image
                if (it.isNotEmpty()) {
                    space_image.visibility = View.GONE
                    no_data_text.visibility = View.GONE
                } else {
                    space_image.visibility = View.VISIBLE
                    no_data_text.visibility = View.VISIBLE
                }
                //updates adapter on new data
                adapter.setRoutes(it)
            }
        })

        route_list.adapter = adapter
        route_list.layoutManager = LinearLayoutManager(this)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?

        var startLocation = Location("Start")
        startLocation.latitude = 0.0
        startLocation.longitude = 0.0

        val locationListener: LocationListener = object : LocationListener {
            val LOCATION_TRACKING = "Location_Tracking"
            override fun onLocationChanged(location: Location) {
                val bearing = startLocation.bearingTo(location)

                Log.d(
                    LOCATION_TRACKING,
                    "Current Longitude : " + startLocation.longitude + "Current Latitude :" + startLocation.latitude + "Bearing : " + startLocation.bearing
                )
                Log.d(
                    LOCATION_TRACKING,
                    "New Longitude : " + location.longitude + "New Latitude :" + location.latitude + "Bearing : " + location.bearing
                )
                Log.d(LOCATION_TRACKING, "BEARING Difference: $bearing")

                //starting point
                if (routeList.size == 0){
                    val routeLocation = location.latitude.toString() + "_" + location.longitude.toString()
                    routeList.add(routeLocation)
                }

                //running
                if (location.bearing > 0f) {
                    val bearingDifference = Math.abs(startLocation.bearing - location.bearing)

                    //val degree = Math.abs(startLocation.bearing - location.bearing) % 360
                    //val rotation = if (degree > 180) 360 - degree else degree
                    //Records only at turns
                    if (bearingDifference > 20) {
                        val routeLocation = location.latitude.toString() + "_" + location.longitude.toString()
                        if (routeList.size < 32)
                            routeList.add(routeLocation)
                    }
                }
                //stoppage
                if (startLocation.bearing == 0f && location.bearing == 0f){
                        val routeLocation = location.latitude.toString() + "_" + location.longitude.toString()
                        if (routeList.size < 32)
                            routeList.add(routeLocation)
                }
                startLocation = location
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        var startTime: Calendar? = null
        fab.setOnClickListener {
            isStarted = if (!isStarted) {
                locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 2f, locationListener)
                startTime = Calendar.getInstance()
                fab.setImageResource(R.drawable.ic_pause_24dp)
                true
            } else {
                locationManager!!.removeUpdates(locationListener)

                val endTime: Calendar = Calendar.getInstance()

                //no progress until at least 2 records are made
                if (routeList.size > 1) {
                    var distance = 0f
                    var startPoint = Location("start")
                    val location: List<String> = routeList[0].split("_")
                    startPoint.latitude = location[0].toDouble()
                    startPoint.longitude = location[1].toDouble()

                    routeList.forEach {
                        val locationItem: List<String> = it.split("_")
                        val nextLocation = Location("next")
                        nextLocation.latitude = locationItem[0].toDouble()
                        nextLocation.longitude = locationItem[1].toDouble()
                        distance += startPoint.distanceTo(nextLocation)
                        startPoint = nextLocation
                    }
                    distance /= 1000


                    val time = endTime.timeInMillis - startTime!!.timeInMillis
                    val startTimeFormat = SimpleDateFormat("h:mm a", Locale.ENGLISH).format(startTime!!.time)
                    val endTimeTimeFormat = SimpleDateFormat("h:mm a", Locale.ENGLISH).format(endTime.time)

                    val route =
                        Route(
                            routeList.toString(),
                            distance,
                            startTimeFormat,
                            endTimeTimeFormat,
                            time,
                            "empty note"
                        )
                    routeViewModel.insert(route)
                    routeList.clear()
                }
                fab.setImageResource(R.drawable.ic_play_arrow_24dp)
                false
            }
        }
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), 99
            )
        }
    }
}
