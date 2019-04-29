package org.zeromod.routehistory

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.squareup.picasso.Picasso
import org.zeromod.routehistory.room.Route
import java.util.concurrent.TimeUnit

class RouteListAdapter internal constructor(context: Context) :
    RecyclerView.Adapter<RouteListAdapter.RouteViewHolder>() {

    inner class RouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val routeDistanceView: TextView = itemView.findViewById(R.id.route_distance)
        val routeTimeView: TextView = itemView.findViewById(R.id.route_time)
        val listItem: LinearLayout = itemView.findViewById(R.id.route_list_item)
        val mapImage: ImageView = itemView.findViewById(R.id.map_image)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var routes = emptyList<Route>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_item, parent, false)
        return RouteViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return routes.size
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        val current = routes[position]
        val distance = current.distance.toShort().toString() + " Kms"
        val time = TimeUnit.MILLISECONDS.toHours(current.duration).toString() + " hr " + (TimeUnit.MILLISECONDS.toMinutes(current.duration) % 60).toString() + " min"
        holder.routeDistanceView.text = distance
        holder.routeTimeView.text = time


        var location = current.location
        location = location.removePrefix("[")
        location = location.removeSuffix("]")

        val routeList: List<String> = location.split(",")

        val startLocation: List<String> = routeList[0].split("_")
        val endLocation: List<String> = routeList[routeList.size - 1].split("_")

        var mapUrl = "https://image.maps.api.here.com/mia/1.6/route"
        var routeCoords = ""
        routeList.forEach{
            val routeItem = it.split("_")
            routeCoords = routeCoords + "," + routeItem[0] + "," + routeItem[1]
        }
        routeCoords = routeCoords.removeRange(0,1)
        
        mapUrl += "?r0=$routeCoords"
        mapUrl += "&m0=" + startLocation[0] + "," + startLocation[1] + "," + endLocation[0] + "," + endLocation[1]
        mapUrl = "$mapUrl&app_id=&app_code="
        Picasso.get().load(mapUrl).into(holder.mapImage)

        holder.listItem.setOnClickListener {
            val intent = Intent(it.context, RouteViewActivity::class.java)
            intent.putStringArrayListExtra("LOCATION",ArrayList(routeList))
            intent.putExtra("ROUTE_ID",current.id)
            intent.putExtra("START_LATITUDE", startLocation[0].toDouble())
            intent.putExtra("START_LONGITUDE", startLocation[1].toDouble())
            intent.putExtra("END_LATITUDE", endLocation[0].toDouble())
            intent.putExtra("END_LONGITUDE", endLocation[1].toDouble())
            intent.putExtra("DISTANCE", distance)
            intent.putExtra("START_TIME", current.startTime)
            intent.putExtra("END_TIME", current.endTime)
            intent.putExtra("DURATION", time)
            it.context.startActivity(intent)
        }
    }

    internal fun setRoutes(routes: List<Route>) {
        this.routes = routes
        notifyDataSetChanged()
    }


}