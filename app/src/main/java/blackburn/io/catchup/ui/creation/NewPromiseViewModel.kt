package blackburn.io.catchup.ui.creation

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import blackburn.io.catchup.app.BaseViewModel
import blackburn.io.catchup.service.app.DataService
import blackburn.io.catchup.service.app.SchedulerUtil
import java.util.*
import javax.inject.Inject

class NewPromiseViewModel @Inject constructor(
  private val scheduler: SchedulerUtil,
  private val data: DataService
): BaseViewModel() {

  val name: MutableLiveData<String> = MutableLiveData()
  val address: MutableLiveData<String> = MutableLiveData()
  val placeInfo: MutableLiveData<PlaceInfo> = MutableLiveData()
  val dateTime: MutableLiveData<Date> = MutableLiveData()
  val contacts: MutableLiveData<List<String>> = MutableLiveData()

  fun getPromise(id: String) {

    name.value = ""
  }

  data class PlaceInfo(val address: String, val latitude: Double, val longitude: Double)
}