package blackburn.io.catchup.ui.entrance

import android.arch.lifecycle.MutableLiveData
import blackburn.io.catchup.app.BaseViewModel
import blackburn.io.catchup.app.Define
import blackburn.io.catchup.service.app.DataService
import blackburn.io.catchup.service.app.SchedulerUtil
import blackburn.io.catchup.service.app.SharedPrefService
import com.amazonaws.amplify.generated.graphql.CreateCatchUpUserMutation
import com.amazonaws.amplify.generated.graphql.UpdateCatchUpUserMutation
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class EntranceViewModel @Inject constructor(
  private val scheduler: SchedulerUtil,
  private val data: DataService,
  private val pref: SharedPrefService
) : BaseViewModel() {

  val initDoneWithPhone: MutableLiveData<String> = MutableLiveData()

  fun updateCatchUpUser(
    id: String,
    phone: String?,
    email: String?,
    nickname: String?,
    profileImagePath: String?,
    gender: String?,
    birthday: String?,
    ageRange: String?,
    credit: Int?
  ) {
    compositeDisposable += data.requestUser(id).compose(scheduler.forObservable()).switchMap {
      pref.userId = id
      pref.nickname = nickname ?: "None"

      if (it.data()?.catchUpUser?.id().isNullOrEmpty()) {
        return@switchMap data.createUser(
          id, phone, email, nickname, profileImagePath, gender, birthday, ageRange
        )
      } else {
        return@switchMap data.updateUser(
          id, phone, email, nickname, profileImagePath, gender, birthday, ageRange, credit
        )
      }
    }.compose(scheduler.forObservable()).switchMap {
      val operationData = it.data()
      val phoneNumber: String
      val contactNickname: String?
      val imagePath: String?

      when (operationData) {
        is CreateCatchUpUserMutation.Data -> {
          phoneNumber = requireNotNull(operationData.createCatchUpUser()?.phone())
          contactNickname = operationData.createCatchUpUser()?.nickname()
          imagePath = operationData.createCatchUpUser()?.profileImagePath()
        }
        is UpdateCatchUpUserMutation.Data -> {
          phoneNumber = requireNotNull(operationData.updateCatchUpUser()?.phone())
          contactNickname = operationData.updateCatchUpUser()?.nickname()
          imagePath = operationData.updateCatchUpUser()?.profileImagePath()
        }
        else -> throw Exception("Exception message")
      }

      return@switchMap data.updateContact(
        phoneNumber,
        contactNickname,
        imagePath,
        null,
        Define.PLATFORM_ANDROID
      )
    }.compose(scheduler.forObservable()).subscribeBy(
      onNext = {
        val phoneNumber = it?.data()?.updateCatchUpContact()?.phone()
        pref.phone = phoneNumber ?: ""
        initDoneWithPhone.value = phoneNumber
      },
      onError = {
        it.printStackTrace()
      }
    )
  }

  fun attachToken(phone: String, pushToken: String) {
    compositeDisposable += data.attachToken(phone, pushToken)
      .subscribeBy(
        onNext = {

        },
        onError = {
          it.printStackTrace()
        }
      )
  }
}