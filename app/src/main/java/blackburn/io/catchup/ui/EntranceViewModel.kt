package blackburn.io.catchup.ui

import android.arch.lifecycle.MutableLiveData
import blackburn.io.catchup.app.BaseViewModel
import blackburn.io.catchup.app.Define
import blackburn.io.catchup.app.plusAssign
import blackburn.io.catchup.service.app.DataService
import blackburn.io.catchup.service.app.SchedulerUtil
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
    compositeDisposable += dataService.requestAppVersion().compose(scheduler.forObservable())
      .subscribeBy(
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
        })
  }

  fun updateCatchUpUser(
    id: Long,
    phone: String?,
    email: String?,
    nickname: String?,
    profileImagePath: String?,
    gender: String?,
    birthday: String?,
    ageRange: String?,
    credit: Int?
  ) {
    compositeDisposable += dataService.updateUser(
      "$id", phone, email, nickname, profileImagePath, gender, birthday, ageRange, credit
    ).switchMap {
      val phoneNumber = requireNotNull(it.data()?.updateCatchUpUser()?.phone())
      return@switchMap dataService.attachToken(phoneNumber, "")
    }.compose(scheduler.forObservable())
      .subscribeBy(
      onNext = {

      },
      onError = {
        it.printStackTrace()
      }
    )
  }

  data class AppVersion(val major: Int, val minor: Int, val revision: Int)
}