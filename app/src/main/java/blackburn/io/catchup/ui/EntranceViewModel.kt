package blackburn.io.catchup.ui

import android.arch.lifecycle.MutableLiveData
import blackburn.io.catchup.app.BaseViewModel
import blackburn.io.catchup.app.Define
import blackburn.io.catchup.service.DataService
import blackburn.io.catchup.service.SchedulerUtil
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class EntranceViewModel @Inject constructor(
  private val scheduler: SchedulerUtil,
  private val dataService: DataService
): BaseViewModel() {

  // Output
  val appVersion: MutableLiveData<AppVersion> = MutableLiveData()

  // Input

  // Action
  fun checkAppVersion() {
    compositeDisposable.add(
      dataService.requestAppVersion().compose(scheduler.forObservable()).subscribeBy(
        onNext = { response ->
          response.data()?.checkAppVersion()?.let {
            appVersion.value = AppVersion(
              it.major() ?: Define.VERSION_MAJOR,
              it.minor() ?: Define.VERSION_MINOR,
              it.revision() ?: Define.VERSION_REVISION
            )
          }
        },
        onError = {
          it.printStackTrace()
        }
      )
    )
  }

  data class AppVersion(val major: Int, val minor: Int, val revision: Int)
}