package blackburn.io.catchup.ui

import android.arch.lifecycle.MutableLiveData
import blackburn.io.catchup.app.BaseViewModel
import blackburn.io.catchup.app.Define
import blackburn.io.catchup.app.plusAssign
import blackburn.io.catchup.service.app.DataService
import blackburn.io.catchup.service.app.SchedulerUtil
import blackburn.io.catchup.service.app.SharedPrefService
import com.google.firebase.iid.FirebaseInstanceId
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class EntranceViewModel @Inject constructor(
  private val scheduler: SchedulerUtil,
  private val data: DataService,
  private val sharedPref: SharedPrefService
): BaseViewModel() {

  // Output
  val appVersion: MutableLiveData<AppVersion> = MutableLiveData()

  // Input

  // Action
  fun checkAppVersion() {
    compositeDisposable += data.requestAppVersion().compose(scheduler.forObservable())
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
    compositeDisposable += data.updateUser(
      "$id", phone, email, nickname, profileImagePath, gender, birthday, ageRange, credit
    ).switchMap {
      val contact = it.data()?.updateCatchUpUser()
      val phoneNumber = requireNotNull(contact?.phone())

      return@switchMap data.updateContact(
        phoneNumber,
        contact?.nickname(),
        contact?.profileImagePath(),
        if (sharedPref.pushToken.isNotEmpty()) sharedPref.pushToken else null,
        Define.PLATFORM_ANDROID
      )
    }.compose(scheduler.forObservable())
      .subscribeBy(
      onNext = {
//        it.data()?.updateCatchUpContact()?.
      },
      onError = {
        it.printStackTrace()
      }
    )
  }

  data class AppVersion(val major: Int, val minor: Int, val revision: Int)
}