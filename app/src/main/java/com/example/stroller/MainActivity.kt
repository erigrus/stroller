package com.example.stroller


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.stroller.util.GeoPoint
import com.example.stroller.util.distance
import com.example.stroller.util.intersect
import com.example.stroller.util.translate
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private var radius = 0
    private var wayPointsSize = 0
    private var randomness = 0.0

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        generate_button.setOnClickListener {
            generateStroll()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        updateLocation()

        radius_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                radius = i
                radius_value.text = i.toString()
            }

            // Not implemented
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        waypoints_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                wayPointsSize = progress
                waypoints_value.text = wayPointsSize.toString()
            }

            // Not implemented
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        randomness_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, i: Int, fromUser: Boolean) {
                randomness = i.toDouble() / 100.0
                randomness_value.text = String.format("%.2f", randomness)
            }

            // Not implemented
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun updateLocation()
    {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(applicationContext, "Location permission missing.", Toast.LENGTH_SHORT)
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if(location != null)
                {
                    lastLocation = location
                }
            }
    }

    private fun generateStroll()
    {
        updateLocation()
        val wayPoints = generateWaypoints()

        var mapsUrl = "https://www.google.com/maps/dir/"
        mapsUrl += "'" + lastLocation.latitude.toString() + "," + lastLocation.longitude + "'/"

        for(wayPoint in wayPoints)
        {
            mapsUrl += "'" + wayPoint.lat.toString() + "," + wayPoint.lon + "'/"
        }

        mapsUrl += "'" + lastLocation.latitude.toString() + "," + lastLocation.longitude + "'/"

        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl))
        startActivity(browserIntent)
    }

    private fun generateWaypoints() : List<GeoPoint>
    {
        // create list and add start point
        val wayPoints = mutableListOf<GeoPoint>()
        val startEndPoint = GeoPoint(lastLocation.latitude, lastLocation.longitude)
        wayPoints.add(startEndPoint)

        // start heading
        var heading = Random.nextDouble(0.0, 360.0)

        for (i in 1..wayPointsSize)
        {

            var valid = false
            var newPoint = GeoPoint(0.0,0.0)

            while (!valid)
            {
                val transDist = Random.nextInt(100,500)

                // create new direction and wrap it around 360 degrees
                heading += Random.nextDouble(-180.0, 180.0)
                heading %= 360

                // create new waypoint and validate it
                newPoint = translate(wayPoints[wayPoints.size-1], transDist.toDouble(), heading)
                valid = wayPoints.size < 3
                        || (distance(startEndPoint, newPoint) <= radius)
                        //&& (!intersect(listOf(newPoint, wayPoints[wayPoints.size-1]), wayPoints)))
            }

            wayPoints.add(newPoint)
        }

        // add endpoint
        wayPoints.add(startEndPoint)

        return wayPoints
    }
}
