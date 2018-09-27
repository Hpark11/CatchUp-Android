package blackburn.io.catchup.service.android

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.google.android.gms.location.*
import java.util.*

class LocationTrackingService: Service(), LocationListener {
  // lateinit var locationClient: GoogleApiClient
  private val locationRequest = LocationRequest()

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//    locationClient = GoogleApiClient.Builder(this)
//      .addConnectionCallbacks(this)
//      .addOnConnectionFailedListener(this)
//      .addApi(LocationServices.API)
//      .build()

    locationRequest.interval = LOCATION_INTERVAL
    locationRequest.fastestInterval = LOCATION_FAST_INTERVAL
    locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    // locationClient.connect()

    if (ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
      ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
    ) {
      return Service.START_STICKY
    }

    val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, locationListener)

    LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, object: LocationCallback() {
      override fun onLocationAvailability(availability: LocationAvailability?) {
        super.onLocationAvailability(availability)
      }

      override fun onLocationResult(result: LocationResult?) {
        val location = result?.locations?.first()
        super.onLocationResult(result)
        updateLocation(location)
      }
    }, null)

    return Service.START_STICKY
  }

  private fun updateLocation(location: Location?) {
//    val shared = SharedDataService(this@LocationTrackingService)
//    val closestTime = shared.closestTime
//    val phone = shared.phone
//
//    val current = Calendar.getInstance().timeInMillis
////    val availableAt = current - 7200000L
//
//    if (current < closestTime - 7200000L) return
//
//    location?.let {
//      if (phone.isNotEmpty()) {
//        App.apolloClient.mutate(
//          RelocatePocketMutation.builder().phone(phone).latitude(it.latitude).longitude(it.longitude).build()
//        ).enqueue(object: ApolloCall.Callback<RelocatePocketMutation.Data> () {
//          override fun onResponse(response: Response<RelocatePocketMutation.Data>) {
//
//          }
//
//          override fun onFailure(e: ApolloException) {
//            e.printStackTrace()
//          }
//        })
//      }
//    }
  }

  val locationListener = object : android.location.LocationListener {
    override fun onLocationChanged(location: Location) {
      updateLocation(location)
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
      val a = 2
    }

    override fun onProviderEnabled(provider: String) {
      val a = 2
    }

    override fun onProviderDisabled(provider: String) {
      val a = 2
    }
  }

  override fun onBind(intent: Intent?): IBinder? {
    return null;
  }

  override fun onLocationChanged(location: Location?) {
    location?.let {
      sendLocationInfo(location.latitude, location.longitude);
      Log.d(TAG, "Latitude: ${location.latitude}, Longitude: ${location.longitude}")
    }
  }

  private fun sendLocationInfo(lat: Double, lng: Double) {
    val intent = Intent(ACTION_LOCATION_BROADCAST)
    intent.putExtra(EXTRA_LATITUDE, lat)
    intent.putExtra(EXTRA_LONGITUDE, lng)
    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
  }

  companion object {
    private val TAG = LocationTrackingService::class.java.simpleName
    val ACTION_LOCATION_BROADCAST = "${LocationTrackingService::class.java.name}LocationBroadcast"
    val EXTRA_LATITUDE = "extra_latitude"
    val EXTRA_LONGITUDE = "extra_longitude"

    private val LOCATION_INTERVAL = 9000L
    private val LOCATION_FAST_INTERVAL = 7000L
    private val LOCATION_DISTANCE = 10f
  }
}