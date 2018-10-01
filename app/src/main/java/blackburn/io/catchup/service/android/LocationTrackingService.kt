package blackburn.io.catchup.service.android

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import android.util.Log
import blackburn.io.catchup.service.app.DataService
import blackburn.io.catchup.service.app.SharedPrefService
import com.google.android.gms.location.*
import dagger.android.DaggerService
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class LocationTrackingService: DaggerService() {
  companion object {
    private val LOCATION_INTERVAL = 15000L
    private val LOCATION_FAST_INTERVAL = 9000L
    private val LOCATION_DISTANCE = 10f
  }

  private val locationRequest = LocationRequest()
  private val compositeDisposable = CompositeDisposable()

  @Inject
  lateinit var dataService: DataService

  @Inject
  lateinit var prefService: SharedPrefService

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

    locationRequest.interval = LOCATION_INTERVAL
    locationRequest.fastestInterval = LOCATION_FAST_INTERVAL
    locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

    if (ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
      ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
    ) {
      return Service.START_STICKY
    }

    LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, object: LocationCallback() {
      override fun onLocationAvailability(availability: LocationAvailability?) {
        super.onLocationAvailability(availability)
      }

      override fun onLocationResult(result: LocationResult?) {
        super.onLocationResult(result)
        updateLocation(result?.locations?.first())
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
    location?.let { location ->
      if (prefService.phone.isNotEmpty()) {
        compositeDisposable += dataService.relocateContact(prefService.phone, location.latitude, location.longitude)
          .subscribeBy(
            onNext = {
              Log.d("LOCATION TRACKING", "${location.latitude} ${location.longitude}")
            },
            onError = {
              it.printStackTrace()
            }
          )
      }
    }
  }

  override fun onBind(intent: Intent?): IBinder? {
    return null
  }

  override fun onDestroy() {
    super.onDestroy()
  }

  override fun onUnbind(intent: Intent?): Boolean {
    return super.onUnbind(intent)
  }
}