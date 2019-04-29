package org.zeromod.routehistory

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.here.android.mpa.common.GeoCoordinate
import com.here.android.mpa.common.OnEngineInitListener
import com.here.android.mpa.mapping.*
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.routing.*

import kotlinx.android.synthetic.main.activity_route_view.*
import org.zeromod.routehistory.room.Route
import org.zeromod.routehistory.room.RouteViewModel

class RouteViewActivity : AppCompatActivity() {
    lateinit var map: Map
    lateinit var supportMapFragment: SupportMapFragment
    val TAG = "ROUTE_ENGINE"
    lateinit var startMarker: MapMarker
    lateinit var endMarker: MapMarker
    private lateinit var routeViewModel: RouteViewModel
    var route: Route? = null

    @SuppressLint("InflateParams")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_view)

        routeViewModel = ViewModelProviders.of(this).get(RouteViewModel::class.java)
        val location: ArrayList<String> = intent.getStringArrayListExtra("LOCATION")

        val startLatitude = intent.getDoubleExtra("START_LATITUDE", 0.0)
        val startLongitude = intent.getDoubleExtra("START_LONGITUDE", 0.0)
        val endLatitude = intent.getDoubleExtra("END_LATITUDE", 0.0)
        val endLongitude = intent.getDoubleExtra("END_LONGITUDE", 0.0)

        val routeManager = RouteManager()
        val routePlan = RoutePlan()
        val routeOptions = RouteOptions()
        routeOptions.transportMode = RouteOptions.TransportMode.CAR
        routeOptions.routeType = RouteOptions.Type.SHORTEST
        routePlan.routeOptions = routeOptions

        val startLocation = GeoCoordinate(startLatitude, startLongitude)
        val endLocation = GeoCoordinate(endLatitude, endLongitude)

        location.forEach {
            val wayPoints: List<String> = it.split("_")
            routePlan.addWaypoint(GeoCoordinate(wayPoints[0].toDouble(), wayPoints[1].toDouble()))
        }

        class RouteListener : RouteManager.Listener {
            override fun onCalculateRouteFinished(p0: RouteManager.Error?, p1: MutableList<RouteResult>?) {
                if (p0 == RouteManager.Error.NONE) {
                    val mapRoute = MapRoute(p1!![0].route)
                    map.addMapObject(mapRoute)

                    startMarker = MapMarker()
                    startMarker.coordinate = startLocation

                    endMarker = MapMarker()
                    endMarker.coordinate = endLocation
                    map.addMapObject(startMarker)
                    map.addMapObject(endMarker)
                } else {
                    Log.d(TAG, "Error calculating route $p0")
                }
            }

            override fun onProgress(p0: Int) {
                Log.d(TAG, "Calculating percentage : $p0")
            }

        }

        routeManager.calculateRoute(routePlan, RouteListener())

        supportMapFragment = supportFragmentManager.findFragmentById(R.id.mapfragment) as SupportMapFragment
        supportMapFragment.init {
            if (it == OnEngineInitListener.Error.NONE) {
                map = supportMapFragment.map
                map.setCenter((GeoCoordinate(startLatitude, startLongitude)), Map.Animation.LINEAR)
                map.zoomLevel = (map.maxZoomLevel + map.minZoomLevel) / 2
            } else {
                Log.d(TAG, "MAP Engine error")
            }
        }
        val distance = "Distance : " + intent.getStringExtra("DISTANCE")
        route_distance.text = distance

        val duration = "Duration : " + intent.getStringExtra("DURATION")
        route_time.text = duration

        val startTime = "Start Time : " + intent.getStringExtra("START_TIME")
        route_start_time.text = startTime

        val endTime = "End Time : " + intent.getStringExtra("END_TIME")
        route_end_time.text = endTime

        val routeId = intent.getIntExtra("ROUTE_ID",0)
        val routeLiveData: LiveData<Route> = routeViewModel.getRouteById(routeId)


        routeLiveData.observe(this, Observer {
            route = it!!
        })

        notes_button.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this).create()
            val layoutInflater = this.layoutInflater

            val dialogView: View = layoutInflater.inflate(R.layout.notes_view, null)

            val editText = dialogView.findViewById<EditText>(R.id.notes_text)
            editText.setText(route?.notes)

            val dismissButton = dialogView.findViewById<Button>(R.id.notes_ok_button)
            dismissButton.setOnClickListener {
                if (route?.notes!!.compareTo(editText.text.toString()) != 0){
                    route!!.notes = editText.text.toString()
                    routeViewModel.update(route!!)
                }
                dialogBuilder.dismiss()
            }
            dialogBuilder.setView(dialogView)
            dialogBuilder.show()

        }


    }

}
