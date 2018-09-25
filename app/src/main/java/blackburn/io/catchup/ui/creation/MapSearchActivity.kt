package blackburn.io.catchup.ui.creation

import android.os.Bundle
import android.util.Log
import blackburn.io.catchup.R
import blackburn.io.catchup.app.BaseWithoutDIActivity
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_map_search.*

class MapSearchActivity: BaseWithoutDIActivity(), OnMapReadyCallback {

  private var selectedAddress: String = ""
  private var selectedLatitude: Double = 0.0
  private var selectedLongitude: Double = 0.0

  private var googleMap: GoogleMap? = null
  private var currentMarker: Marker? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_map_search)

    val mapFragment = fragmentManager.findFragmentById(R.id.locationSearchMap) as MapFragment
    mapFragment.getMapAsync(this)
    val autocompleteFragment = fragmentManager.findFragmentById(R.id.place_autocomplete_fragment) as PlaceAutocompleteFragment

    autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
      override fun onPlaceSelected(place: Place) {
        selectedAddress = place.address.toString()
        selectedLatitude = place.latLng.latitude
        selectedLongitude = place.latLng.longitude

        val markerOptions = MarkerOptions()
        markerOptions.position(place.latLng)
        markerOptions.title(place.name.toString())
        markerOptions.snippet(place.address.toString())

        currentMarker?.remove()
        currentMarker = googleMap?.addMarker(markerOptions)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLng(place.latLng))
        googleMap?.animateCamera(CameraUpdateFactory.zoomTo(16f))
      }

      override fun onError(status: Status) {
        Log.i("GOogleMap", "An error occurred: $status")
      }
    })
  }

  override fun onMapReady(map: GoogleMap?) {
    googleMap = map
    val seoul = LatLng(37.56, 126.97)

    selectedAddress = "대한민국 서울"
    selectedLatitude = seoul.latitude
    selectedLongitude = seoul.longitude

    val markerOptions = MarkerOptions()
    markerOptions.position(seoul)
    markerOptions.title("서울")
    markerOptions.snippet("한국의 수도")

    currentMarker = map?.addMarker(markerOptions)
    map?.moveCamera(CameraUpdateFactory.newLatLng(seoul))
    map?.animateCamera(CameraUpdateFactory.zoomTo(10f))

    confirmLocationButton.setOnClickListener {
      intent.putExtra("latitude", selectedLatitude)
      intent.putExtra("longitude", selectedLongitude)
      intent.putExtra("address", selectedAddress)
      setResult(RESULT_CODE_SET_ADDRESS, intent)
      finish()
    }
  }

  companion object {
    val RESULT_CODE_SET_ADDRESS = 9998
  }
}
