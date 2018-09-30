package blackburn.io.catchup.ui.detail

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import blackburn.io.catchup.R
import blackburn.io.catchup.app.BaseFragment
import blackburn.io.catchup.di.module.GlideApp
import blackburn.io.catchup.model.PlaceInfo
import com.amazonaws.amplify.generated.graphql.BatchGetCatchUpContactsQuery
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.fragment_promise_detail_map.view.*
import kotlinx.android.synthetic.main.view_promise_marker.view.*

class PromiseDetailMapFragment : BaseFragment() {

  private var googleMap: GoogleMap? = null
  private var destinationMarker: Marker? = null
  private val currentMemberMarkers = mutableMapOf<String, Marker>()

  private var promiseName = ""
  private var placeInfo = PlaceInfo("대한민국 서울", 37.56, 126.97)
  private var contactList = listOf<BatchGetCatchUpContactsQuery.BatchGetCatchUpContact>()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.fragment_promise_detail_map, container, false)

    view.promiseDetailMapView.onCreate(savedInstanceState)
    view.promiseDetailMapView.onResume()

    try {
      MapsInitializer.initialize(activity?.applicationContext)
    } catch (e: Exception) {
      e.printStackTrace()
    }

    view.promiseDetailMapView.getMapAsync {
      googleMap = it

      updateDestination(promiseName, placeInfo)
      updateContacts(contactList)
    }

    return view
  }

  fun updateDestination(name: String, place: PlaceInfo) {
    placeInfo = place
    promiseName = name

    googleMap?.let {
      setDestination(
        createMarker(name, place.address, place.latitude, place.longitude)
      )
    }
  }

  fun updateContacts(contacts: List<BatchGetCatchUpContactsQuery.BatchGetCatchUpContact>) {
    contactList = contacts

    googleMap?.let {
      contacts.forEach { contact ->
        val phone = contact.phone()
        val name = contact.nickname() ?: ""
        val latitude = contact.latitude() ?: 0.0
        val longitude = contact.longitude() ?: 0.0
        val profileImagePath = contact.profileImagePath() ?: ""

        if (currentMemberMarkers[phone] == null) {
          setCurrentMemberMarker(
            phone,
            profileImagePath,
            createMarker(name, "", latitude, longitude)
          )
        } else {
          currentMemberMarkers[phone]?.position = LatLng(latitude, longitude)
        }
      }
    }
  }

  private fun setDestination(markerOptions: MarkerOptions) {
    destinationMarker = googleMap?.addMarker(markerOptions)
    googleMap?.moveCamera(CameraUpdateFactory.newLatLng(markerOptions.position))
    googleMap?.animateCamera(CameraUpdateFactory.zoomTo(10f))
  }

  private fun setCurrentMemberMarker(phone: String, imagePath: String, markerOptions: MarkerOptions) {
    googleMap?.addMarker(markerOptions)?.let { marker ->
      val markerView = LayoutInflater.from(activity).inflate(R.layout.view_promise_marker, null)
      marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmapFrom(markerView)))

      val target = object : SimpleTarget<Bitmap>() {
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
          markerView.userProfileImageView.setImageBitmap(resource)
          currentMemberMarkers[phone]?.setIcon(
            BitmapDescriptorFactory.fromBitmap(bitmapFrom(markerView))
          )
        }
      }

      GlideApp.with(this)
        .asBitmap()
        .load(imagePath)
        .apply(RequestOptions.bitmapTransform(CropCircleTransformation()))
        .override(80)
        .placeholder(R.drawable.profile_default)
        .into(target)

      currentMemberMarkers[phone] = marker
    }
  }

  private fun bitmapFrom(view: View): Bitmap {
    val displayMetrics = DisplayMetrics()
    activity!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
    view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
    view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
    view.buildDrawingCache()
    val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
    view.draw(Canvas(bitmap))
    return bitmap
  }

  private fun createMarker(name: String, address: String, lat: Double, lng: Double): MarkerOptions {
    val markerOptions = MarkerOptions()
    val place = LatLng(lat, lng)
    markerOptions.position(place)
    markerOptions.title(name)
    markerOptions.snippet(address)
    return markerOptions
  }
}