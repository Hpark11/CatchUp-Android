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
import com.amazonaws.amplify.generated.graphql.GetCatchUpPromiseQuery
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

class PromiseDetailMapFragment: BaseFragment() {

  private var googleMap: GoogleMap? = null
  private var destinationMarker: Marker? = null
  private val currentMemberMarkers = mutableMapOf<String, Marker>()

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

      val seoul = LatLng(37.56, 126.97)
      val markerOptions = MarkerOptions()
      markerOptions.position(seoul)
      markerOptions.title("서울")
      markerOptions.snippet("한국의 수도")
      googleMap?.moveCamera(CameraUpdateFactory.newLatLng(seoul))
      googleMap?.animateCamera(CameraUpdateFactory.zoomTo(10f))
    }

    return view
  }

  fun updateCurrentInfo(promise: GetCatchUpPromiseQuery.GetCatchUpPromise) {
    if (googleMap == null) return

//    promise?.let {
//      if (destinationMarker == null) {
//        setDestination(
//          createMarker(
//            it.name() ?: "",
//            it.address() ?: "",
//            it.latitude() ?: 0.0,
//            it.longitude() ?: 0.0
//          )
//        )
//      }
//
//      if (currentMemberMarkers.isEmpty()) {
//        it.pockets()?.forEach {
//          setCurrentMemberMarker(
//            it.profileImagePath() ?: "",
//            createMarker(
//              it.nickname() ?: "",
//              "",
//              it.latitude() ?: 0.0,
//              it.longitude() ?: 0.0
//            )
//          )
//        }
//      } else {
//        it.pockets()?.forEach { pocket ->
//          currentMemberMarkers[pocket.profileImagePath()]?.let {
//            it.position = LatLng(pocket.latitude() ?: 0.0, pocket.longitude() ?: 0.0)
//          }
//        }
//      }
//    }
  }

  private fun setDestination(markerOptions: MarkerOptions) {
    destinationMarker = googleMap?.addMarker(markerOptions)
    googleMap?.moveCamera(CameraUpdateFactory.newLatLng(markerOptions.position))
    googleMap?.animateCamera(CameraUpdateFactory.zoomTo(10f))
  }

  private fun setCurrentMemberMarker(imagePath: String, markerOptions: MarkerOptions) {
    googleMap?.addMarker(markerOptions)?.let {
      val target = object: SimpleTarget<Bitmap>() {
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
          val markerView = LayoutInflater.from(activity).inflate(R.layout.view_promise_marker, null)
          markerView.userProfileImageView.setImageBitmap(resource)
          currentMemberMarkers[imagePath]?.setIcon(BitmapDescriptorFactory.fromBitmap(bitmapFrom(markerView)))
        }
      }

      GlideApp.with(this)
        .asBitmap()
        .load(imagePath)
        .apply(RequestOptions.bitmapTransform(CropCircleTransformation()))
        .override(80)
        .placeholder(R.drawable.image_place_holder)
        .into(target)

      currentMemberMarkers[imagePath] = it
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